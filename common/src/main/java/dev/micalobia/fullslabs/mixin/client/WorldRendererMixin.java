package dev.micalobia.fullslabs.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.sugar.Local;
import dev.micalobia.fullslabs.util.Utility;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @SuppressWarnings("LocalMayBeArgsOnly") // This warning is erroneous
    @ModifyReceiver(method = "fillEntityOutlineRenderStates", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getOutlineShape(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/ShapeContext;)Lnet/minecraft/util/shape/VoxelShape;"))
    private BlockState changeRenderedOutline(BlockState state, BlockView view, BlockPos pos, ShapeContext shapeContext, @Local BlockHitResult crosshair) {
        return Utility.targetedHalf(view, state, pos, crosshair.getPos());
    }
}
