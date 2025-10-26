package dev.micalobia.fullslabs.fabric.mixin.compat.mo_glass;

import dev.micalobia.fullslabs.block.VerticalSlabBlock;
import dev.micalobia.fullslabs.block.VerticalSlabBlock.VerticalType;
import dev.micalobia.fullslabs.util.Utility;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Direction;
import net.wurstclient.glass.GlassSlabBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GlassSlabBlock.class)
public class GlassSlabBlockMixin {
    @Inject(method = "isSideInvisible", at = @At(value = "HEAD"), cancellable = true)
    private void addVerticalSlabCheck(BlockState state, BlockState stateFrom, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (stateFrom.isOf(VerticalSlabBlock.getVertical((GlassSlabBlock) (Object) this)))
            cir.setReturnValue(fullslabs$isInvisibleToVerticalSlab(state, stateFrom, direction));
    }

    @Unique
    private boolean fullslabs$isInvisibleToVerticalSlab(BlockState state, BlockState stateFrom, Direction direction) {
        var typeFrom = stateFrom.get(VerticalSlabBlock.TYPE);
        if (typeFrom == VerticalType.FULL) return true;
        var directionFrom = Utility.slabDirection(stateFrom);
        return direction.getOpposite() == directionFrom;
    }
}
