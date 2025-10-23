package dev.micalobia.fullslabs.handlers;

import dev.micalobia.fullslabs.ducks.AxeItemDuck;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BlockState;
import net.minecraft.block.Oxidizable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.HoneycombItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class OxidizableMixedHandler implements MixedHandler {
    public static final OxidizableMixedHandler INSTANCE = new OxidizableMixedHandler();

    private OxidizableMixedHandler() {}

    @Override
    public void randomTick(MixedContext.Sided context, ServerWorld world, BlockPos pos, Random random) {
        var state = context.state();
        if (!(state.getBlock() instanceof Oxidizable oxidizable)) return;
        oxidizable.tryDegrade(state, world, pos, random).ifPresent(s -> context.replaceBlock(s.getBlock()));
    }

    @Override
    public ActionResult onUseWithItem(MixedContext.Sided context, ItemStack stack, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        var item = stack.getItem();
        var state = context.state();
        if (item instanceof AxeItemDuck axe) {
            var stripped = axe.fullslabs$strippedState(world, pos, player, state, new ItemUsageContext(player, hand, hit));
            if (stripped.isPresent()) {
                var success = context.replaceBlock(stripped.get().getBlock());
                return success ? ActionResult.SUCCESS : ActionResult.PASS;
            }
            return ActionResult.PASS;
        }
        if (item instanceof HoneycombItem) return useWaxOnBlock(context, stack, state, world, pos, player);
        return ActionResult.PASS;
    }

    // See HoneycombItem.useOnBlock
    private ActionResult useWaxOnBlock(MixedContext context, ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player) {
        return HoneycombItem.getWaxedState(state).<ActionResult>map(s -> {
            var success = context.replaceBlock(s.getBlock()); // This is the main difference
            if (!success) return ActionResult.PASS;
            if (player instanceof ServerPlayerEntity serverPlayer) {
                Criteria.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, stack);
            }
            stack.decrementUnlessCreative(1, player); // Confused on how HoneycombItem.useOnBlock can get away with just decrement
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(player, state));
            world.syncWorldEvent(player, 3003, pos, 0); // Wax event, spawns particles
            return ActionResult.SUCCESS;
        }).orElse(ActionResult.PASS);
    }
}
