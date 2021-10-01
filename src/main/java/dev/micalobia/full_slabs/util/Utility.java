package dev.micalobia.full_slabs.util;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import dev.micalobia.full_slabs.FullSlabsMod;
import dev.micalobia.full_slabs.mixin.block.SlabBlockAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.shape.VoxelShape;
import virtuoel.statement.api.StateRefresher;

public class Utility {
	public static final VoxelShape TOP_SHAPE;
	public static final VoxelShape BOTTOM_SHAPE;
	public static final VoxelShape NORTH_SHAPE;
	public static final VoxelShape EAST_SHAPE;
	public static final VoxelShape SOUTH_SHAPE;
	public static final VoxelShape WEST_SHAPE;
	private static Pair<Block, Block> fullSlabGhost;
	private static Pair<Block, BlockItem> extraSlabGhost;

	static {
		TOP_SHAPE = SlabBlockAccessor.getTOP_SHAPE();
		BOTTOM_SHAPE = SlabBlockAccessor.getBOTTOM_SHAPE();
		NORTH_SHAPE = Block.createCuboidShape(0d, 0d, 0d, 16d, 16d, 8d);
		SOUTH_SHAPE = Block.createCuboidShape(0d, 0d, 8d, 16d, 16d, 16d);
		WEST_SHAPE = Block.createCuboidShape(0d, 0d, 0d, 8d, 16d, 16d);
		EAST_SHAPE = Block.createCuboidShape(8d, 0d, 0d, 16d, 16d, 16d);
	}

	public static Direction getDirection(SlabType type, Axis axis) {
		return switch(type) {
			case TOP -> Direction.get(AxisDirection.POSITIVE, axis);
			case BOTTOM -> Direction.get(AxisDirection.NEGATIVE, axis);
			case DOUBLE -> throw new IllegalArgumentException("Slab type 'DOUBLE' is directionless!");
		};
	}

	public static VoxelShape getShape(Direction direction) {
		return switch(direction) {
			case NORTH -> Utility.NORTH_SHAPE;
			case EAST -> Utility.EAST_SHAPE;
			case SOUTH -> Utility.SOUTH_SHAPE;
			case WEST -> Utility.WEST_SHAPE;
			case UP -> Utility.TOP_SHAPE;
			case DOWN -> Utility.BOTTOM_SHAPE;
		};
	}

	public static boolean isSlabBlock(ItemStack stack) {
		return stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof SlabBlock;
	}

	public static SlabType slabType(Direction direction) {
		return direction.getDirection() == AxisDirection.POSITIVE ? SlabType.TOP : SlabType.BOTTOM;
	}

	public static boolean contains(VoxelShape shape, Vec3d pos) {
		for(Box box : shape.getBoundingBoxes())
			if(box.contains(pos)) return true;
		return false;
	}

	public static boolean borders(VoxelShape shape, Vec3d pos, Axis axis) {
		double value = pos.getComponentAlongAxis(axis);
		for(Box box : shape.getBoundingBoxes()) {
			double min = box.getMin(axis);
			double max = box.getMax(axis);
			if(equalToEither(value, min, max)) return true;
		}
		return false;
	}

	public static boolean equalToEither(double self, double left, double right) {
		if(MathHelper.approximatelyEquals(self, left)) return true;
		return MathHelper.approximatelyEquals(self, right);
	}

	private static boolean isPositiveX(Vec3d hit, BlockPos pos) {
		return hit.getX() - pos.getX() > 0.5d;
	}

	private static boolean isPositiveY(Vec3d hit, BlockPos pos) {
		return hit.getY() - pos.getY() > 0.5d;
	}

	private static boolean isPositiveZ(Vec3d hit, BlockPos pos) {
		return hit.getZ() - pos.getZ() > 0.5d;
	}

	public static boolean isPositive(Axis axis, Vec3d hit, BlockPos pos) {
		return switch(axis) {
			case X -> isPositiveX(hit, pos);
			case Y -> isPositiveY(hit, pos);
			case Z -> isPositiveZ(hit, pos);
		};
	}


	public static HitResult crosshair(PlayerEntity player) {
		return player.raycast(player.isCreative() ? 5.0d : 4.5d, 0f, false);
	}

	public static boolean tilted(Identifier id) {
		return FullSlabsMod.TILTED_SLABS.contains(id);
	}

	public static Identifier getBlockId(Block block) {
		return Registry.BLOCK.getId(block);
	}

	public static Block getBlock(Identifier id) {
		return Registry.BLOCK.get(id);
	}

	public static Direction getDirection(Axis axis, Vec3d hit, BlockPos pos) {
		return switch(axis) {
			case X -> isPositiveX(hit, pos) ? Direction.EAST : Direction.WEST;
			case Y -> isPositiveY(hit, pos) ? Direction.UP : Direction.DOWN;
			case Z -> isPositiveZ(hit, pos) ? Direction.SOUTH : Direction.NORTH;
		};
	}

	private static double modOne(double value) {
		return value - Math.floor(value);
	}

	public static boolean insideSlab(Block block, Vec3d pos) {
		if(block instanceof SlabBlock) {
			if(modOne(pos.getX()) == 0d) return false;
			if(modOne(pos.getY()) == 0d) return false;
			return modOne(pos.getZ()) != 0d;
		}
		return false;
	}

	public static BlockState getSlabState(Pair<Block, Block> pair, Axis axis, Vec3d hit, BlockPos pos) {
		return getSlabState(pair, axis, isPositive(axis, hit, pos));
	}

	public static BlockState getSlabState(Pair<Block, Block> pair, Axis axis, boolean positive) {
		return positive ?
				pair.getFirst().getDefaultState().with(Properties.AXIS, axis).with(SlabBlock.TYPE, SlabType.TOP) :
				pair.getSecond().getDefaultState().with(Properties.AXIS, axis).with(SlabBlock.TYPE, SlabType.BOTTOM);
	}

	public static Pair<Block, Block> getFullSlabGhost() {
		return fullSlabGhost;
	}

	public static void setFullSlabGhost(Block positive, Block negative) {
		fullSlabGhost = Pair.of(positive, negative);
	}

	public static Pair<Block, BlockItem> getExtraSlabGhost() {
		return extraSlabGhost;
	}

	public static void setExtraSlabGhost(Block base, BlockItem extra) {
		extraSlabGhost = Pair.of(base, extra);
	}

	public static <T extends Comparable<T>> void injectBlockProperty(Class<? extends Block> cls, Property<T> property, T defaultValue) {
		for(Block block : Registry.BLOCK) {
			if(cls.isAssignableFrom(block.getClass())) {
				injectBlockProperty(block, property, defaultValue);
			}
		}
	}

	public static <T extends Comparable<T>> void injectBlockProperty(Block block, Property<T> property, T defaultValue) {
		StateRefresher.INSTANCE.addBlockProperty(block, property, defaultValue);
	}

	// All code below is ported and optionally modified from Malilib RenderUtils/PositionUtils

	public static HitPart getHitPart(Direction originalSide, Direction playerFacingH, BlockPos pos, Vec3d hitVec) {
		Vec3d positions = getHitPartPositions(originalSide, playerFacingH, pos, hitVec);
		double posH = positions.x;
		double posV = positions.y;
		double offH = Math.abs(posH - 0.5d);
		double offV = Math.abs(posV - 0.5d);

		if(offH > 0.25d || offV > 0.25d) {
			if(offH > offV) {
				return posH < 0.5d ? HitPart.LEFT : HitPart.RIGHT;
			} else {
				return posV < 0.5d ? HitPart.BOTTOM : HitPart.TOP;
			}
		} else {
			return HitPart.CENTER;
		}
	}

	private static Vec3d getHitPartPositions(Direction originalSide, Direction playerFacingH, BlockPos pos, Vec3d hitVec) {
		double x = hitVec.x - pos.getX();
		double y = hitVec.y - pos.getY();
		double z = hitVec.z - pos.getZ();
		double posH = 0;
		double posV = 0;

		switch(originalSide) {
			case DOWN, UP -> {
				switch(playerFacingH) {
					case NORTH:
						posH = x;
						posV = 1.0d - z;
						break;
					case SOUTH:
						posH = 1.0d - x;
						posV = z;
						break;
					case WEST:
						posH = 1.0d - z;
						posV = 1.0d - x;
						break;
					case EAST:
						posH = z;
						posV = x;
						break;
					default:
				}
				if(originalSide == Direction.DOWN) {
					posV = 1.0d - posV;
				}
			}
			case NORTH, SOUTH -> {
				posH = originalSide.getDirection() == AxisDirection.POSITIVE ? x : 1.0d - x;
				posV = y;
			}
			case WEST, EAST -> {
				posH = originalSide.getDirection() == AxisDirection.NEGATIVE ? z : 1.0d - z;
				posV = y;
			}
		}

		return new Vec3d(posH, posV, 0);
	}

	public static Direction getTargetedDirection(Direction side, Direction playerFacingH, BlockPos pos, Vec3d hitVec) {
		Vec3d positions = getHitPartPositions(side, playerFacingH, pos, hitVec);
		double posH = positions.x;
		double posV = positions.y;
		double offH = Math.abs(posH - 0.5d);
		double offV = Math.abs(posV - 0.5d);

		if(offH > 0.25d || offV > 0.25d)
			if(side.getAxis() == Direction.Axis.Y)
				if(offH > offV)
					return posH < 0.5d ? playerFacingH.rotateYCounterclockwise() : playerFacingH.rotateYClockwise();
				else if(side == Direction.DOWN)
					return posV > 0.5d ? playerFacingH.getOpposite() : playerFacingH;
				else
					return posV < 0.5d ? playerFacingH.getOpposite() : playerFacingH;
			else if(offH > offV)
				return posH < 0.5d ? side.rotateYClockwise() : side.rotateYCounterclockwise();
			else
				return posV < 0.5d ? Direction.DOWN : Direction.UP;
		return side;
	}

	public static void renderBlockTargetingOverlay(Entity entity, BlockPos pos, Direction side, Vec3d hitVec,
												   BlockState state, MinecraftClient mc) {
		Direction playerFacing = entity.getHorizontalFacing();
		HitPart part = getHitPart(side, playerFacing, pos, hitVec);
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
			Direction dir = getDirection(state.get(SlabBlock.TYPE), state.get(Properties.AXIS));
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

	public enum HitPart {
		CENTER,
		LEFT,
		RIGHT,
		BOTTOM,
		TOP
	}

	private enum HitPartQuad {
		CENTER_FULL,
		CENTER_TOP,
		CENTER_LEFT,
		CENTER_RIGHT,
		CENTER_BOTTOM,
		TOP_FULL,
		TOP_LEFT,
		TOP_RIGHT,
		LEFT_FULL,
		LEFT_TOP,
		LEFT_BOTTOM,
		RIGHT_FULL,
		RIGHT_TOP,
		RIGHT_BOTTOM,
		BOTTOM_FULL,
		BOTTOM_LEFT,
		BOTTOM_RIGHT
	}
}
