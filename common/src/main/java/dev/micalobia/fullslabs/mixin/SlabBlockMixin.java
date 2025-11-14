package dev.micalobia.fullslabs.mixin;

import dev.micalobia.fullslabs.SlabRegistry;
import dev.micalobia.fullslabs.block.MixedSlabBlock;
import dev.micalobia.fullslabs.block.MixedSlabBlock.MixedType;
import dev.micalobia.fullslabs.block.VerticalSlabBlock;
import dev.micalobia.fullslabs.block.VerticalSlabBlock.VerticalType;
import dev.micalobia.fullslabs.config.Controls;
import dev.micalobia.fullslabs.handlers.MixedHandlers;
import dev.micalobia.fullslabs.util.SlabPlacement;
import dev.micalobia.fullslabs.util.Utility;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(SlabBlock.class)
public class SlabBlockMixin {
    @Inject(method = "getStateForPlacement", at = @At("HEAD"), cancellable = true)
    private void editPlacementRules(BlockPlaceContext ctx, CallbackInfoReturnable<BlockState> cir) {
        var self = fullslabs$self();
        if (!VerticalSlabBlock.hasVertical(self)) return;
        var pos = ctx.getClickedPos();
        var world = ctx.getLevel();
        var state = world.getBlockState(pos);
        if (state.is(VerticalSlabBlock.getVertical(self)))
            cir.setReturnValue(state.setValue(VerticalSlabBlock.TYPE, VerticalType.FULL).setValue(BlockStateProperties.WATERLOGGED, false));
        else if (state.is(self))
            cir.setReturnValue(state.setValue(BlockStateProperties.SLAB_TYPE, SlabType.DOUBLE).setValue(BlockStateProperties.WATERLOGGED, false));
        else if (Utility.isSlab(state)) {
            cir.setReturnValue(SlabRegistry.MIXED_SLAB.defaultBlockState().setValue(MixedSlabBlock.TYPE, MixedType.fromState(state)));
        } else {
            var face = ctx.getClickedFace();
            var fluidState = world.getFluidState(pos);
            var player = ctx.getPlayer();
            final Direction target;
            if (player == null) target = Direction.DOWN;
            else {
                var mode = Controls.getPlacementMode(ctx.getPlayer().getUUID());
                target = SlabPlacement.getTargetedDirection(mode, face, ctx.getHorizontalDirection(), pos, ctx.getClickLocation());
            }
            cir.setReturnValue(Utility.getTargetedState(fullslabs$self(), face, target, ctx.getRotation()).setValue(BlockStateProperties.WATERLOGGED, fluidState.is(Fluids.WATER)));
        }
    }

    @Inject(method = "canBeReplaced", at = @At("HEAD"), cancellable = true)
    private void editReplacementRules(BlockState state, BlockPlaceContext context, CallbackInfoReturnable<Boolean> cir) {
        var self = fullslabs$self();
        if (!VerticalSlabBlock.hasVertical(self)) return;
        // This keeps the code a bit tidier imo
        validate:
        {
            var stack = context.getItemInHand();
            var type = state.getValue(BlockStateProperties.SLAB_TYPE);
            if (type == SlabType.DOUBLE) break validate;
            if (!(stack.getItem() instanceof BlockItem blockItem)) break validate;
            var block = blockItem.getBlock();
            if (!(block instanceof SlabBlock)) break validate;
            if (block != self && !(MixedHandlers.hasHandler(block) && MixedHandlers.hasHandler(self))) break validate;
            if (context.replacingClickedOnBlock()) {
                cir.setReturnValue(Utility.isInsideSlab(state, context.getClickedPos(), context.getClickLocation()));
                return;
            }
            cir.setReturnValue(true);
            return;
        }
        cir.setReturnValue(false);
    }

    @Unique
    private SlabBlock fullslabs$self() {
        return (SlabBlock) (Object) this;
    }
}
