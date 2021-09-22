package dev.micalobia.full_slabs;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.micalobia.full_slabs.util.Utility;
import fi.dy.masa.malilib.interfaces.IRenderer;
import fi.dy.masa.malilib.render.RenderUtils;
import fi.dy.masa.malilib.util.Color4f;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.Matrix4f;

public class OverlayRenderer implements IRenderer {
	@Override
	public void onRenderWorldLast(MatrixStack matrix, Matrix4f projMatrix) {
		MinecraftClient mc = MinecraftClient.getInstance();

		if(mc.player != null) this.renderSlabOverlay(matrix, mc);
	}

	public void renderSlabOverlay(MatrixStack matrix, MinecraftClient mc) {
		Entity entity = mc.getCameraEntity();

		assert mc.player != null;
		boolean hasSlab = Utility.isSlabBlock(mc.player.getMainHandStack()) || Utility.isSlabBlock(mc.player.getOffHandStack());

		if(hasSlab && mc.crosshairTarget != null && mc.crosshairTarget.getType() == Type.BLOCK) {
			BlockHitResult hitResult = (BlockHitResult) mc.crosshairTarget;
			assert mc.world != null;
			BlockState state = mc.world.getBlockState(hitResult.getBlockPos());
			if(Utility.insideSlab(state.getBlock(), hitResult.getPos())) return;

			RenderSystem.depthMask(false);
			RenderSystem.disableCull();
			RenderSystem.disableTexture();
			RenderSystem.disableDepthTest();

			RenderUtils.setupBlend();

			Color4f color = new Color4f(0f, .5f, 1f, .25f);
			assert entity != null;
			RenderUtils.renderBlockTargetingOverlay(
					entity, hitResult.getBlockPos(), hitResult.getSide(), hitResult.getPos(), color, matrix, mc
			);

			RenderSystem.enableTexture();
			RenderSystem.enableDepthTest();
			RenderSystem.disableBlend();
			RenderSystem.enableCull();
			RenderSystem.depthMask(true);
		}
	}
}
