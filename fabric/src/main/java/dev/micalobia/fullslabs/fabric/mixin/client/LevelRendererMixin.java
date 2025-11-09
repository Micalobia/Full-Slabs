package dev.micalobia.fullslabs.fabric.mixin.client;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.micalobia.fullslabs.client.BlockFaceOverlay;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.state.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Definition(id = "endLastBatch", method = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endLastBatch()V")
    @Expression("?.endLastBatch()")
    @Inject(method = "renderBlockOutline", at = @At("MIXINEXTRAS:EXPRESSION"))
    private void drawOverlay(MultiBufferSource.BufferSource immediate, PoseStack matrices, boolean renderBlockOutline, LevelRenderState renderStates, CallbackInfo ci) {
        BlockFaceOverlay.renderFaceOverlay(renderStates);
    }
}
