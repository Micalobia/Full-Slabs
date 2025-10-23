package dev.micalobia.fullslabs.mixin.client;

import dev.micalobia.fullslabs.util.Utility;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BlockRenderManager.class)
public class BlockRenderManagerMixin {
    @ModifyVariable(method = "renderDamage", at = @At("HEAD"), argsOnly = true)
    private BlockState changeSlabDamageRender(BlockState state, BlockState ignored, BlockPos pos, BlockRenderView view) {
        var hit = MinecraftClient.getInstance().crosshairTarget;
        if (!(hit instanceof BlockHitResult)) return state;
        return Utility.targetedHalf(view, state, pos, hit.getPos());
    }
}
