package dev.micalobia.fullslabs.fabric.mixin.client;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import dev.micalobia.fullslabs.client.BlockFaceOverlay;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.state.WorldRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Shadow
    private ClientWorld world;

    @Definition(id = "drawCurrentLayer", method = "Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;drawCurrentLayer()V")
    @Expression("?.drawCurrentLayer()")
    @Inject(method = "renderTargetBlockOutline", at = @At("MIXINEXTRAS:EXPRESSION"))
    private void drawOverlay(VertexConsumerProvider.Immediate immediate, MatrixStack matrices, boolean renderBlockOutline, WorldRenderState renderStates, CallbackInfo ci) {
        BlockFaceOverlay.renderFaceOverlay(renderStates);
    }
}
