package dev.micalobia.fullslabs.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.sugar.Local;
import dev.micalobia.fullslabs.util.Utility;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @SuppressWarnings("LocalMayBeArgsOnly") // This warning is erroneous
    @ModifyReceiver(method = "extractBlockOutline", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;"))
    private BlockState changeRenderedOutline(BlockState state, BlockGetter view, BlockPos pos, CollisionContext shapeContext, @Local BlockHitResult crosshair) {
        return Utility.targetedHalf(view, state, pos, crosshair.getLocation());
    }
}
