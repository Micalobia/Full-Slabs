package dev.micalobia.full_slabs.util;

import com.mojang.datafixers.util.Pair;
import dev.micalobia.full_slabs.mixin.block.SlabBlockAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

public class Utility {
	public static final VoxelShape TOP_OUTLINE_SHAPE;
	public static final VoxelShape BOTTOM_OUTLINE_SHAPE;
	public static final VoxelShape NORTH_OUTLINE_SHAPE;
	public static final VoxelShape EAST_OUTLINE_SHAPE;
	public static final VoxelShape SOUTH_OUTLINE_SHAPE;
	public static final VoxelShape WEST_OUTLINE_SHAPE;
	public static final VoxelShape TOP_COLLISION_SHAPE;
	public static final VoxelShape BOTTOM_COLLISION_SHAPE;
	public static final VoxelShape NORTH_COLLISION_SHAPE;
	public static final VoxelShape EAST_COLLISION_SHAPE;
	public static final VoxelShape SOUTH_COLLISION_SHAPE;
	public static final VoxelShape WEST_COLLISION_SHAPE;
	private static Pair<Block, Block> ghostPair;

	static {
		TOP_OUTLINE_SHAPE = Block.createCuboidShape(0.0D, 7.95D, 0.0D, 16.0D, 16.0D, 16.0D);
		BOTTOM_OUTLINE_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 8.05D, 16.0D);
		NORTH_OUTLINE_SHAPE = Block.createCuboidShape(0d, 0d, 0d, 16d, 16d, 8.05d);
		SOUTH_OUTLINE_SHAPE = Block.createCuboidShape(0d, 0d, 7.95d, 16d, 16d, 16d);
		WEST_OUTLINE_SHAPE = Block.createCuboidShape(0d, 0d, 0d, 8.05d, 16d, 16d);
		EAST_OUTLINE_SHAPE = Block.createCuboidShape(7.95d, 0d, 0d, 16d, 16d, 16d);

		TOP_COLLISION_SHAPE = SlabBlockAccessor.getTOP_SHAPE();
		BOTTOM_COLLISION_SHAPE = SlabBlockAccessor.getBOTTOM_SHAPE();
		NORTH_COLLISION_SHAPE = Block.createCuboidShape(0d, 0d, 0d, 16d, 16d, 8d);
		SOUTH_COLLISION_SHAPE = Block.createCuboidShape(0d, 0d, 8d, 16d, 16d, 16d);
		WEST_COLLISION_SHAPE = Block.createCuboidShape(0d, 0d, 0d, 8d, 16d, 16d);
		EAST_COLLISION_SHAPE = Block.createCuboidShape(8d, 0d, 0d, 16d, 16d, 16d);
	}

	public static Direction getDirection(SlabType type, Axis axis) {
		return switch(type) {
			case TOP -> Direction.get(AxisDirection.POSITIVE, axis);
			case BOTTOM -> Direction.get(AxisDirection.NEGATIVE, axis);
			case DOUBLE -> throw new IllegalArgumentException("Slab type 'DOUBLE' is directionless!");
		};
	}

	public static boolean isSlabBlock(ItemStack stack) {
		return stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof SlabBlock;
	}

	public static SlabType slabType(Direction direction) {
		return direction.getDirection() == AxisDirection.POSITIVE ? SlabType.TOP : SlabType.BOTTOM;
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

	public static Direction getDirection(Axis axis, Vec3d hit, BlockPos pos) {
		return switch(axis) {
			case X -> isPositiveX(hit, pos) ? Direction.EAST : Direction.WEST;
			case Y -> isPositiveY(hit, pos) ? Direction.UP : Direction.DOWN;
			case Z -> isPositiveZ(hit, pos) ? Direction.SOUTH : Direction.NORTH;
		};
	}

	public static Direction generateSlab(HitPart hitPart, Direction hitSide, Direction facing) {
		if(hitSide.getAxis().isHorizontal())
			return switch(hitPart) {
				case CENTER -> hitSide.getOpposite();
				case BOTTOM -> Direction.DOWN;
				case TOP -> Direction.UP;
				case LEFT -> hitSide.rotateYClockwise();
				case RIGHT -> hitSide.rotateYCounterclockwise();
			};
		else {
			return switch(hitPart) {
				case CENTER -> hitSide.getOpposite();
				case BOTTOM -> hitSide == Direction.UP ? facing.getOpposite() : facing;
				case TOP -> hitSide == Direction.UP ? facing : facing.getOpposite();
				case LEFT -> facing.rotateYCounterclockwise();
				case RIGHT -> facing.rotateYClockwise();
			};
		}
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

	public static Pair<Block, Block> getGhostPair() {
		return ghostPair;
	}

	public static void setGhostPair(Pair<Block, Block> pair) {
		ghostPair = pair;
	}

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

	public enum HitPart {
		CENTER,
		LEFT,
		RIGHT,
		BOTTOM,
		TOP
	}
}
