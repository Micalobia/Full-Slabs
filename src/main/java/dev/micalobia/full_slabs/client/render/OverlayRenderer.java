package dev.micalobia.full_slabs.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.micalobia.full_slabs.block.SlabBlockUtility;
import dev.micalobia.full_slabs.config.CustomControls;
import fi.dy.masa.malilib.event.RenderEventHandler;
import fi.dy.masa.malilib.interfaces.IRenderer;
import fi.dy.masa.malilib.render.RenderUtils;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.Matrix4f;

public class OverlayRenderer implements IRenderer {
	public static void init() {
		IRenderer renderer = new OverlayRenderer();
		RenderEventHandler.getInstance().registerWorldLastRenderer(renderer);
	}

	@Override
	public void onRenderWorldLast(MatrixStack matrix, Matrix4f projMatrix) {
		MinecraftClient mc = MinecraftClient.getInstance();

		if(mc.player != null) this.renderSlabOverlay(mc);
	}

	public void renderSlabOverlay(MinecraftClient mc) {
		if(!CustomControls.getShowWidget()) return;
		Entity entity = mc.getCameraEntity();

		assert mc.player != null;
		boolean hasSlab = SlabBlockUtility.isSlabBlock(mc.player.getMainHandStack()) || SlabBlockUtility.isSlabBlock(mc.player.getOffHandStack());

		if(hasSlab && mc.crosshairTarget != null && mc.crosshairTarget.getType() == Type.BLOCK) {
			BlockHitResult hitResult = (BlockHitResult) mc.crosshairTarget;
			assert mc.world != null;
			BlockState state = mc.world.getBlockState(hitResult.getBlockPos());
			if(SlabBlockUtility.insideSlab(state.getBlock(), hitResult.getPos())) return;

			RenderSystem.depthMask(false);
			RenderSystem.disableCull();
			RenderSystem.disableTexture();
			RenderSystem.disableDepthTest();

			RenderUtils.setupBlend();

			assert entity != null;
			if(CustomControls.getVerticalEnabled(mc.player.getUuid()))
				RenderUtility.renderBlockTargetingOverlay(
						entity, hitResult.getBlockPos(), hitResult.getSide(), hitResult.getPos(), state, mc
				);
			else
				RenderUtility.renderBlockVerticalHalfOverlay(
						entity, hitResult.getBlockPos(), hitResult.getSide(), hitResult.getPos(), state, mc
				);

			RenderSystem.enableTexture();
			RenderSystem.enableDepthTest();
			RenderSystem.disableBlend();
			RenderSystem.enableCull();
			RenderSystem.depthMask(true);
		}
	}
}
