package dev.micalobia.fullslabs.handlers;

import dev.micalobia.fullslabs.ducks.AxeItemDuck;
import dev.micalobia.fullslabs.util.SlabContext;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

public class OxidizableMixedHandler implements MixedHandler {
    public static final OxidizableMixedHandler INSTANCE = new OxidizableMixedHandler();

    private OxidizableMixedHandler() {}

    @Override
    public void randomTick(SlabContext context, ServerLevel world, BlockPos pos, RandomSource random) {
        var state = context.mainState();
        if (!(state.getBlock() instanceof WeatheringCopper oxidizable)) return;
        oxidizable.getNextState(state, world, pos, random).ifPresent(s -> context.replaceMain(world, s.getBlock()));
    }

    @Override
    public InteractionResult useItemOn(SlabContext context, ItemStack stack, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        var item = stack.getItem();
        var state = context.mainState();
        if (item instanceof AxeItemDuck axe) {
            var stripped = axe.fullslabs$strippedState(world, pos, player, state, new UseOnContext(player, hand, hit));
            if (stripped.isPresent()) {
                var success = context.replaceMain(world, stripped.get().getBlock());
                return success ? InteractionResult.SUCCESS : InteractionResult.PASS;
            }
            return InteractionResult.PASS;
        }
        if (item instanceof HoneycombItem) return useWaxOnBlock(context, stack, state, world, pos, player);
        return InteractionResult.PASS;
    }

    // See HoneycombItem.useOnBlock
    private InteractionResult useWaxOnBlock(SlabContext context, ItemStack stack, BlockState state, Level world, BlockPos pos, Player player) {
        return HoneycombItem.getWaxed(state).<InteractionResult>map(s -> {
            var success = context.replaceMain(world, s.getBlock()); // This is the main difference
            if (!success) return InteractionResult.PASS;
            if (player instanceof ServerPlayer serverPlayer) {
                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, stack);
            }
            stack.consume(1, player); // Confused on how HoneycombItem.useOnBlock can get away with just decrement
            world.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, state));
            world.levelEvent(player, 3003, pos, 0); // Wax event, spawns particles
            return InteractionResult.SUCCESS;
        }).orElse(InteractionResult.PASS);
    }
}
