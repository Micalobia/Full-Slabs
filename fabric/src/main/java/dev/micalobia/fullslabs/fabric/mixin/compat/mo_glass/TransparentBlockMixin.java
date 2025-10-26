package dev.micalobia.fullslabs.fabric.mixin.compat.mo_glass;

import dev.micalobia.fullslabs.block.VerticalSlabBlock;
import dev.micalobia.fullslabs.util.Utility;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.TransparentBlock;
import net.minecraft.util.math.Direction;
import net.wurstclient.glass.MoGlassBlocks;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TransparentBlock.class, priority = 2000)
public class TransparentBlockMixin {
    @Dynamic
    @Inject(method = "isSideInvisible", at = @At(value = "TAIL"), cancellable = true)
    private void addVerticalSlabCheck(BlockState state, BlockState stateFrom, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (state.isOf(Blocks.GLASS) && stateFrom.isOf(VerticalSlabBlock.getVertical((SlabBlock) MoGlassBlocks.GLASS_SLAB)))
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
