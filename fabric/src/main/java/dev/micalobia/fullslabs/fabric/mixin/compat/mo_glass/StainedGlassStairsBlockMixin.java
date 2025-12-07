package dev.micalobia.fullslabs.fabric.mixin.compat.mo_glass;

import dev.micalobia.fullslabs.block.SlabLike;
import dev.micalobia.fullslabs.fabric.compat.mo_glass.StainedGlassVerticalSlabBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import net.wurstclient.glass.StainedGlassStairsBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StainedGlassStairsBlock.class)
public abstract class StainedGlassStairsBlockMixin {
    @Shadow
    public abstract DyeColor getColor();

    @Inject(method = "skipRendering", at = @At(value = "HEAD"), cancellable = true, require = 0)
    private void addVerticalSlabCheck(BlockState state, BlockState stateFrom, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        var blockFrom = stateFrom.getBlock();
        if (blockFrom instanceof StainedGlassVerticalSlabBlock stained && this.getColor() == stained.getColor())
            cir.setReturnValue(fullslabs$isInvisibleToVerticalSlab(state, stateFrom, direction));
    }

    @Unique
    private boolean fullslabs$isInvisibleToVerticalSlab(BlockState state, BlockState stateFrom, Direction direction) {
        var slab = (SlabLike) stateFrom.getBlock();
        if (slab.isDouble(stateFrom)) return true;
        var directionFrom = slab.getDirection(stateFrom);
        return direction.getOpposite() == directionFrom;
    }
}
