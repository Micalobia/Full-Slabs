package dev.micalobia.fullslabs.mixin;

import dev.micalobia.fullslabs.block.VerticalSlabBlock;
import dev.micalobia.fullslabs.block.VerticalSlabBlock.VerticalType;
import dev.micalobia.fullslabs.util.Utility;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.fluid.Fluids;
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
    private void fullslabs$editPlacementRules(ItemPlacementContext ctx, CallbackInfoReturnable<BlockState> cir) {
        var self = fullslabs$self();
        if (!VerticalSlabBlock.hasVertical(self)) return;
        var pos = ctx.getBlockPos();
        var world = ctx.getWorld();
        var state = world.getBlockState(pos);
        if (state.isOf(VerticalSlabBlock.getVertical(self)))
            cir.setReturnValue(state.with(VerticalSlabBlock.TYPE, VerticalType.FULL).with(Properties.WATERLOGGED, false));
        else if (state.isOf(self))
            cir.setReturnValue(state.with(SlabBlock.TYPE, SlabType.DOUBLE).with(Properties.WATERLOGGED, false));
        else {
            var face = ctx.getSide();
            var fluidState = world.getFluidState(pos);
            var target = Utility.getTargetedDirection(face, ctx.getHorizontalPlayerFacing(), pos, ctx.getHitPos());
            cir.setReturnValue(Utility.getTargetedState(fullslabs$self(), face, target, ctx.getPlayerYaw()).with(Properties.WATERLOGGED, fluidState.isOf(Fluids.WATER)));
        }
    }

    @Unique
    private SlabBlock fullslabs$self() {
        return (SlabBlock) (Object) this;
    }
}
