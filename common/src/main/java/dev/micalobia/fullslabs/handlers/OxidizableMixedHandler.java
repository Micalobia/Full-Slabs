package dev.micalobia.fullslabs.handlers;

import dev.micalobia.fullslabs.SlabRegistry;
import dev.micalobia.fullslabs.block.SlabLike;
import dev.micalobia.fullslabs.ducks.AxeItemDuck;
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
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!(state.getBlock() instanceof WeatheringCopper oxidizable)) return;
        var mixedState = level.getBlockState(pos);
        var slab = (SlabLike) state.getBlock();
        oxidizable.getNextState(state, level, pos, random).ifPresent(
                s -> SlabRegistry.MIXED_SLAB.replaceHalf(mixedState, level, pos, slab.isTowards(state), s.getBlock())
        );
    }

    @Override
    public InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        var item = stack.getItem();
        if (item instanceof AxeItemDuck axe) {
            var slab = (SlabLike) state.getBlock();
            var mixedState = level.getBlockState(pos);
            var stripped = axe.fullslabs$strippedState(level, pos, player, state, new UseOnContext(player, hand, hit));
            if (stripped.isPresent()) {
                var success = SlabRegistry.MIXED_SLAB.replaceHalf(mixedState, level, pos, slab.isTowards(state), stripped.get().getBlock());
                return success ? InteractionResult.SUCCESS : InteractionResult.PASS;
            }
            return InteractionResult.PASS;
        }
        if (item instanceof HoneycombItem) return useWaxOnBlock(stack, state, level, pos, player);
        return InteractionResult.PASS;
    }

    // See HoneycombItem.useOn
    private InteractionResult useWaxOnBlock(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player) {
        return HoneycombItem.getWaxed(state).<InteractionResult>map(s -> {
            var mixedState = level.getBlockState(pos);
            var slab = (SlabLike) s.getBlock();
            // This is the main difference
            var success = SlabRegistry.MIXED_SLAB.replaceHalf(mixedState, level, pos, slab.isTowards(s), s.getBlock());
            if (!success) return InteractionResult.PASS;
            if (player instanceof ServerPlayer serverPlayer) {
                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, stack);
            }
            stack.consume(1, player); // Confused on how HoneycombItem.useOnBlock can get away with just shrink
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, state));
            level.levelEvent(player, 3003, pos, 0); // Wax event, spawns particles
            return InteractionResult.SUCCESS;
        }).orElse(InteractionResult.PASS);
    }
}
