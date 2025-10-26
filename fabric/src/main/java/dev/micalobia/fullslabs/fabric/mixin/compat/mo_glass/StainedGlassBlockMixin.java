package dev.micalobia.fullslabs.fabric.mixin.compat.mo_glass;

import dev.micalobia.fullslabs.block.VerticalSlabBlock;
import dev.micalobia.fullslabs.fabric.compat.mo_glass.StainedGlassVerticalSlabBlock;
import dev.micalobia.fullslabs.util.Utility;
import net.minecraft.block.BlockState;
import net.minecraft.block.StainedGlassBlock;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = StainedGlassBlock.class, priority = 2000)
public abstract class StainedGlassBlockMixin {
    @Shadow
    public abstract DyeColor getColor();

    @Dynamic
    @Inject(method = "isSideInvisible", at = @At(value = "TAIL"), cancellable = true)
    private void addVerticalSlabCheck(BlockState state, BlockState stateFrom, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        var block = stateFrom.getBlock();
        if (block instanceof StainedGlassVerticalSlabBlock stained && stained.getColor() == this.getColor())
            cir.setReturnValue(fullslabs$isInvisibleToVerticalSlab(state, stateFrom, direction));
    }

    @Unique
    private boolean fullslabs$isInvisibleToVerticalSlab(BlockState state, BlockState stateFrom, Direction direction) {
        var typeFrom = stateFrom.get(VerticalSlabBlock.TYPE);
        if (typeFrom == VerticalSlabBlock.VerticalType.FULL) return true;
        var directionFrom = Utility.slabDirection(stateFrom);
        return direction.getOpposite() == directionFrom;
    }
}
