package dev.micalobia.full_slabs.client.render;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.micalobia.full_slabs.block.SlabBlockUtility;
import dev.micalobia.full_slabs.util.Utility;
import dev.micalobia.full_slabs.util.malilib.HitPart;
import dev.micalobia.full_slabs.util.malilib.HitPartQuad;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

public class RenderUtility {
	// All code below is ported and modified from Malilib RenderUtils/PositionUtils

	public static void renderBlockVerticalHalfOverlay(Entity entity, BlockPos pos, Direction side, Vec3d hitVec, BlockState state, MinecraftClient mc) {
		Direction playerFacing = entity.getHorizontalFacing();
		Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
		double x = pos.getX() + 0.5d - cameraPos.x;
		double y = pos.getY() + 0.5d - cameraPos.y;
		double z = pos.getZ() + 0.5d - cameraPos.z;

		boolean top = hitVec.getY() - pos.getY() > 0.5f;
		float topOff = top ? 0.5f : 0f;

		MatrixStack globalStack = RenderSystem.getModelViewStack();
		globalStack.push();
		blockTargetingOverlayTranslations(x, y, z, side, playerFacing, globalStack);
		RenderSystem.applyModelViewMatrix();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		RenderSystem.disableTexture();

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
		int hr = 0x00;
		int hg = 0x7F;
		int hb = 0xFF;
		int ha = 0x3F;
		int c = 0xFF;


		if(side.getAxis().isHorizontal()) {
			buffer.vertex(x - 0.5f, y - 0.5f + topOff, z).color(hr, hg, hb, ha).next();
			buffer.vertex(x + 0.5f, y - 0.5f + topOff, z).color(hr, hg, hb, ha).next();
			buffer.vertex(x + 0.5f, y + topOff, z).color(hr, hg, hb, ha).next();
			buffer.vertex(x - 0.5f, y + topOff, z).color(hr, hg, hb, ha).next();
		} else {
			buffer.vertex(x - 0.5f, y - 0.5f, z).color(hr, hg, hb, ha).next();
			buffer.vertex(x + 0.5f, y - 0.5f, z).color(hr, hg, hb, ha).next();
			buffer.vertex(x + 0.5f, y + 0.5f, z).color(hr, hg, hb, ha).next();
			buffer.vertex(x - 0.5f, y + 0.5f, z).color(hr, hg, hb, ha).next();
		}
		tessellator.draw();


		if(side.getAxis().isHorizontal()) {
			RenderSystem.lineWidth(1.6f);
			buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
			buffer.vertex(x - 0.5f, y, z).color(c, c, c, c).next();
			buffer.vertex(x + 0.5f, y, z).color(c, c, c, c).next();
			tessellator.draw();
		}

		globalStack.pop();
	}

	public static void renderBlockTargetingOverlay(Entity entity, BlockPos pos, Direction side, Vec3d hitVec,
												   BlockState state, MinecraftClient mc) {
		Direction playerFacing = entity.getHorizontalFacing();
		HitPart part = Utility.getHitPart(side, playerFacing, pos, hitVec);
		Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();

		double x = pos.getX() + 0.5d - cameraPos.x;
		double y = pos.getY() + 0.5d - cameraPos.y;
		double z = pos.getZ() + 0.5d - cameraPos.z;

		MatrixStack globalStack = RenderSystem.getModelViewStack();
		globalStack.push();
		blockTargetingOverlayTranslations(x, y, z, side, playerFacing, globalStack);
		RenderSystem.applyModelViewMatrix();

		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		RenderSystem.disableTexture();

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		int hr = 0x00;
		int hg = 0x7F;
		int hb = 0xFF;
		int ha = 0x3F;
		int c = 0xFF;

		buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

		HitPartQuad quad;
		HitPart cut;
		if(state.getBlock() instanceof SlabBlock && state.get(SlabBlock.TYPE) != SlabType.DOUBLE) {
			Direction dir = SlabBlockUtility.getDirection(state.get(SlabBlock.TYPE), state.get(Properties.AXIS));
			cut = getCut(dir, playerFacing, side);
		} else cut = HitPart.CENTER;
		quad = getHitQuad(part, cut);

		ImmutableList<Vec3d> vecs = hitQuads(quad);

		for(Vec3d v : vecs)
			buffer.vertex(x + v.getX(), y + v.getY(), z + v.getZ()).color(hr, hg, hb, ha).next();
		tessellator.draw();

		RenderSystem.lineWidth(1.6f);
		buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

		vecs = hitLines(cut);

		for(Vec3d v : vecs)
			buffer.vertex(x + v.getX(), y + v.getY(), z + v.getZ()).color(c, c, c, c).next();
		tessellator.draw();

		globalStack.pop();
	}

	private static void blockTargetingOverlayTranslations(double x, double y, double z, Direction side, Direction playerFacing, MatrixStack matrixStack) {
		matrixStack.translate(x, y, z);

		switch(side) {
			case DOWN:
				matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180f - playerFacing.asRotation()));
				matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(90f));
				break;
			case UP:
				matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180f - playerFacing.asRotation()));
				matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(-90f));
				break;
			case NORTH:
				matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180f));
				break;
			case SOUTH:
				break;
			case WEST:
				matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-90f));
				break;
			case EAST:
				matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(90f));
				break;
		}

		matrixStack.translate(-x, -y, -z + 0.510);
	}

	private static Vec3d vec(double x, double y) {
		return new Vec3d(x, y, 0);
	}

	public static HitPart getCut(Direction slab, Direction facing, Direction hitFace) {
		if(slab == hitFace) return HitPart.CENTER;
		Axis hitAxis = hitFace.getAxis();
		boolean hitH = hitAxis.isHorizontal();
		boolean hitV = hitAxis.isVertical();
		if(slab.getAxis().isHorizontal()) {
			if(slab.rotateYClockwise() == hitFace) return HitPart.RIGHT;
			if(slab.rotateYCounterclockwise() == hitFace) return HitPart.LEFT;
			if(hitV) {
				if(slab.rotateYCounterclockwise() == facing)
					return HitPart.RIGHT;
				if(slab.rotateYClockwise() == facing)
					return HitPart.LEFT;
			}
		}
		boolean bottom = hitFace == Direction.DOWN;
		if(hitV && slab == facing) return bottom ? HitPart.BOTTOM : HitPart.TOP;
		if(hitH && slab == Direction.UP) return HitPart.TOP;
		if(hitV && slab == facing.getOpposite()) return bottom ? HitPart.TOP : HitPart.BOTTOM;
		if(hitH && slab == Direction.DOWN) return HitPart.BOTTOM;
		throw new IllegalArgumentException("Something has gone very wrong"); // Should be impossible to get to
	}

	private static HitPartQuad getHitQuad(HitPart hitPart, HitPart hitCut) {
		return switch(hitCut) {
			case CENTER -> switch(hitPart) {
				case CENTER -> HitPartQuad.CENTER_FULL;
				case LEFT -> HitPartQuad.LEFT_FULL;
				case RIGHT -> HitPartQuad.RIGHT_FULL;
				case BOTTOM -> HitPartQuad.BOTTOM_FULL;
				case TOP -> HitPartQuad.TOP_FULL;
			};
			case LEFT -> switch(hitPart) {
				case CENTER -> HitPartQuad.CENTER_LEFT;
				case LEFT -> HitPartQuad.LEFT_FULL;
				case RIGHT -> throw new AssertionError();
				case BOTTOM -> HitPartQuad.BOTTOM_LEFT;
				case TOP -> HitPartQuad.TOP_LEFT;
			};
			case RIGHT -> switch(hitPart) {
				case CENTER -> HitPartQuad.CENTER_RIGHT;
				case LEFT -> throw new AssertionError();
				case RIGHT -> HitPartQuad.RIGHT_FULL;
				case BOTTOM -> HitPartQuad.BOTTOM_RIGHT;
				case TOP -> HitPartQuad.TOP_RIGHT;
			};
			case BOTTOM -> switch(hitPart) {
				case CENTER -> HitPartQuad.CENTER_BOTTOM;
				case LEFT -> HitPartQuad.LEFT_BOTTOM;
				case RIGHT -> HitPartQuad.RIGHT_BOTTOM;
				case BOTTOM -> HitPartQuad.BOTTOM_FULL;
				case TOP -> throw new AssertionError();
			};
			case TOP -> switch(hitPart) {
				case CENTER -> HitPartQuad.CENTER_TOP;
				case LEFT -> HitPartQuad.LEFT_TOP;
				case RIGHT -> HitPartQuad.RIGHT_TOP;
				case BOTTOM -> throw new AssertionError();
				case TOP -> HitPartQuad.TOP_FULL;
			};
		};
	}

	private static ImmutableList<Vec3d> hitQuads(HitPartQuad hit) {
		return switch(hit) {
			case CENTER_FULL -> ImmutableList.of(
					vec(-0.25, -0.25),
					vec(+0.25, -0.25),
					vec(+0.25, +0.25),
					vec(-0.25, +0.25));
			case CENTER_TOP -> ImmutableList.of(
					vec(-0.25, -0.00),
					vec(+0.25, -0.00),
					vec(+0.25, +0.25),
					vec(-0.25, +0.25));
			case CENTER_LEFT -> ImmutableList.of(
					vec(-0.25, -0.25),
					vec(+0.00, -0.25),
					vec(+0.00, +0.25),
					vec(-0.25, +0.25));
			case CENTER_RIGHT -> ImmutableList.of(
					vec(-0.00, -0.25),
					vec(+0.25, -0.25),
					vec(+0.25, +0.25),
					vec(-0.00, +0.25));
			case CENTER_BOTTOM -> ImmutableList.of(
					vec(-0.25, -0.25),
					vec(+0.25, -0.25),
					vec(+0.25, +0.00),
					vec(-0.25, +0.00));
			case LEFT_FULL -> ImmutableList.of(
					vec(-0.50, -0.50),
					vec(-0.25, -0.25),
					vec(-0.25, +0.25),
					vec(-0.50, +0.50));
			case LEFT_TOP -> ImmutableList.of(
					vec(-0.50, -0.00),
					vec(-0.25, -0.00),
					vec(-0.25, +0.25),
					vec(-0.50, +0.50));
			case LEFT_BOTTOM -> ImmutableList.of(
					vec(-0.50, -0.50),
					vec(-0.25, -0.25),
					vec(-0.25, +0.00),
					vec(-0.50, +0.00));
			case RIGHT_FULL -> ImmutableList.of(
					vec(+0.50, -0.50),
					vec(+0.25, -0.25),
					vec(+0.25, +0.25),
					vec(+0.50, +0.50));
			case RIGHT_TOP -> ImmutableList.of(
					vec(+0.50, -0.00),
					vec(+0.25, -0.00),
					vec(+0.25, +0.25),
					vec(+0.50, +0.50));
			case RIGHT_BOTTOM -> ImmutableList.of(
					vec(+0.50, -0.50),
					vec(+0.25, -0.25),
					vec(+0.25, +0.00),
					vec(+0.50, +0.00));
			case BOTTOM_FULL -> ImmutableList.of(
					vec(-0.50, -0.50),
					vec(-0.25, -0.25),
					vec(+0.25, -0.25),
					vec(+0.50, -0.50));
			case BOTTOM_LEFT -> ImmutableList.of(
					vec(-0.50, -0.50),
					vec(-0.25, -0.25),
					vec(+0.00, -0.25),
					vec(+0.00, -0.50));
			case BOTTOM_RIGHT -> ImmutableList.of(
					vec(-0.00, -0.50),
					vec(-0.00, -0.25),
					vec(+0.25, -0.25),
					vec(+0.50, -0.50));
			case TOP_FULL -> ImmutableList.of(
					vec(-0.50, +0.50),
					vec(-0.25, +0.25),
					vec(+0.25, +0.25),
					vec(+0.50, +0.50));
			case TOP_LEFT -> ImmutableList.of(
					vec(-0.50, +0.50),
					vec(-0.25, +0.25),
					vec(+0.00, +0.25),
					vec(+0.00, +0.50));
			case TOP_RIGHT -> ImmutableList.of(
					vec(-0.00, +0.50),
					vec(-0.00, +0.25),
					vec(+0.25, +0.25),
					vec(+0.50, +0.50));
		};
	}

	public static ImmutableList<Vec3d> hitLines(HitPart cut) {
		return switch(cut) {
			case CENTER -> ImmutableList.of(
					// Center Bottom
					vec(-0.25, -0.25),
					vec(+0.25, -0.25),
					// Center Right
					vec(+0.25, -0.25),
					vec(+0.25, +0.25),
					// Center Top
					vec(-0.25, +0.25),
					vec(+0.25, +0.25),
					// Center Left
					vec(-0.25, -0.25),
					vec(-0.25, +0.25),
					// Bottom Left
					vec(-0.50, -0.50),
					vec(-0.25, -0.25),
					// Top Left
					vec(-0.50, +0.50),
					vec(-0.25, +0.25),
					// Bottom Right
					vec(+0.50, -0.50),
					vec(+0.25, -0.25),
					// Top Right
					vec(+0.50, +0.50),
					vec(+0.25, +0.25));
			case LEFT -> ImmutableList.of(
					// Center Bottom
					vec(-0.25, -0.25),
					vec(+0.00, -0.25),
					// Center Top
					vec(-0.25, +0.25),
					vec(+0.00, +0.25),
					// Center Left
					vec(-0.25, -0.25),
					vec(-0.25, +0.25),
					// Bottom Left
					vec(-0.50, -0.50),
					vec(-0.25, -0.25),
					// Top Left
					vec(-0.50, +0.50),
					vec(-0.25, +0.25));
			case RIGHT -> ImmutableList.of(
					// Center Bottom
					vec(-0.00, -0.25),
					vec(+0.25, -0.25),
					// Center Right
					vec(+0.25, -0.25),
					vec(+0.25, +0.25),
					// Center Top
					vec(-0.00, +0.25),
					vec(+0.25, +0.25),
					// Bottom Right
					vec(+0.50, -0.50),
					vec(+0.25, -0.25),
					// Top Right
					vec(+0.50, +0.50),
					vec(+0.25, +0.25));
			case BOTTOM -> ImmutableList.of(
					// Center Bottom
					vec(-0.25, -0.25),
					vec(+0.25, -0.25),
					// Center Right
					vec(+0.25, -0.25),
					vec(+0.25, +0.00),
					// Center Left
					vec(-0.25, -0.25),
					vec(-0.25, +0.00),
					// Bottom Left
					vec(-0.50, -0.50),
					vec(-0.25, -0.25),
					// Bottom Right
					vec(+0.50, -0.50),
					vec(+0.25, -0.25));
			case TOP -> ImmutableList.of(
					// Center Right
					vec(+0.25, -0.00),
					vec(+0.25, +0.25),
					// Center Top
					vec(-0.25, +0.25),
					vec(+0.25, +0.25),
					// Center Left
					vec(-0.25, -0.00),
					vec(-0.25, +0.25),
					// Top Left
					vec(-0.50, +0.50),
					vec(-0.25, +0.25),
					// Top Right
					vec(+0.50, +0.50),
					vec(+0.25, +0.25));
		};
	}

}
