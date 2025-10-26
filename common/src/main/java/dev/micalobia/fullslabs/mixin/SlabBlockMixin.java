package dev.micalobia.fullslabs.mixin;

import dev.micalobia.fullslabs.SlabRegistry;
import dev.micalobia.fullslabs.block.MixedSlabBlock;
import dev.micalobia.fullslabs.block.MixedSlabBlock.MixedType;
import dev.micalobia.fullslabs.block.VerticalSlabBlock;
import dev.micalobia.fullslabs.block.VerticalSlabBlock.VerticalType;
import dev.micalobia.fullslabs.handlers.MixedHandlers;
import dev.micalobia.fullslabs.util.SlabPlacement;
import dev.micalobia.fullslabs.util.Utility;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.property.Properties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(SlabBlock.class)
public class SlabBlockMixin {
    @Inject(method = "getPlacementState", at = @At("HEAD"), cancellable = true)
    private void editPlacementRules(ItemPlacementContext ctx, CallbackInfoReturnable<BlockState> cir) {
        var self = fullslabs$self();
        if (!VerticalSlabBlock.hasVertical(self)) return;
        var pos = ctx.getBlockPos();
        var world = ctx.getWorld();
        var state = world.getBlockState(pos);
        if (state.isOf(VerticalSlabBlock.getVertical(self)))
            cir.setReturnValue(state.with(VerticalSlabBlock.TYPE, VerticalType.FULL).with(Properties.WATERLOGGED, false));
        else if (state.isOf(self))
            cir.setReturnValue(state.with(Properties.SLAB_TYPE, SlabType.DOUBLE).with(Properties.WATERLOGGED, false));
        else if (Utility.isSlab(state)) {
            var mixed = SlabRegistry.MIXED_SLAB.get();
            cir.setReturnValue(mixed.getDefaultState().with(MixedSlabBlock.TYPE, MixedType.fromState(state)));
        } else {
            var face = ctx.getSide();
            var fluidState = world.getFluidState(pos);
            var target = SlabPlacement.getTargetedDirection(face, ctx.getHorizontalPlayerFacing(), pos, ctx.getHitPos());
            cir.setReturnValue(Utility.getTargetedState(fullslabs$self(), face, target, ctx.getPlayerYaw()).with(Properties.WATERLOGGED, fluidState.isOf(Fluids.WATER)));
        }
    }

    @Inject(method = "canReplace", at = @At("HEAD"), cancellable = true)
    private void editReplacementRules(BlockState state, ItemPlacementContext context, CallbackInfoReturnable<Boolean> cir) {
        var self = fullslabs$self();
        if (!VerticalSlabBlock.hasVertical(self)) return;
        // This keeps the code a bit tidier imo
        validate:
        {
            var stack = context.getStack();
            var type = state.get(Properties.SLAB_TYPE);
            if (type == SlabType.DOUBLE) break validate;
            var block = ((BlockItem) stack.getItem()).getBlock();
            if (!(block instanceof SlabBlock)) break validate;
            if (block != self && !(MixedHandlers.hasHandler(block) && MixedHandlers.hasHandler(self))) break validate;
            if (context.canReplaceExisting()) {
                cir.setReturnValue(Utility.isInsideSlab(state, context.getBlockPos(), context.getHitPos()));
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
