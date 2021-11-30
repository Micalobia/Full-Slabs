package dev.micalobia.full_slabs.block;

import com.mojang.datafixers.util.Pair;
import dev.micalobia.full_slabs.FullSlabsMod;
import dev.micalobia.full_slabs.mixin.block.SlabBlockAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

public class SlabBlockUtility {
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

	public static VoxelShape getShape(Direction direction) {
		return switch(direction) {
			case NORTH -> NORTH_SHAPE;
			case EAST -> EAST_SHAPE;
			case SOUTH -> SOUTH_SHAPE;
			case WEST -> WEST_SHAPE;
			case UP -> TOP_SHAPE;
			case DOWN -> BOTTOM_SHAPE;
		};
	}

	public static boolean isSlabBlock(ItemStack stack) {
		return stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof SlabBlock;
	}

	public static SlabType slabType(Direction direction) {
		return direction.getDirection() == AxisDirection.POSITIVE ? SlabType.TOP : SlabType.BOTTOM;
	}

	private static boolean isPositiveX(Vec3d hit, BlockPos pos, SlabType primary) {
		if(primary == SlabType.TOP)
			return hit.getX() - pos.getX() >= 0.5d;
		return hit.getX() - pos.getX() > 0.5d;
	}

	private static boolean isPositiveY(Vec3d hit, BlockPos pos, SlabType primary) {
		if(primary == SlabType.TOP)
			return hit.getY() - pos.getY() >= 0.5d;
		return hit.getY() - pos.getY() > 0.5d;
	}

	private static boolean isPositiveZ(Vec3d hit, BlockPos pos, SlabType primary) {
		if(primary == SlabType.TOP)
			return hit.getZ() - pos.getZ() >= 0.5d;
		return hit.getZ() - pos.getZ() > 0.5d;
	}

	public static boolean isPositive(Axis axis, Vec3d hit, BlockPos pos) {
		return isPositive(axis, hit, pos, SlabType.DOUBLE);
	}

	public static boolean isPositive(Axis axis, Vec3d hit, BlockPos pos, SlabType primary) {
		return switch(axis) {
			case X -> isPositiveX(hit, pos, primary);
			case Y -> isPositiveY(hit, pos, primary);
			case Z -> isPositiveZ(hit, pos, primary);
		};
	}

	public static boolean tilted(Identifier id) {
		return FullSlabsMod.TILTED_SLABS.contains(id);
	}

	public static Direction getDirection(Axis axis, Vec3d hit, BlockPos pos) {
		return getDirection(axis, hit, pos, SlabType.DOUBLE);
	}

	public static Direction getDirection(Axis axis, Vec3d hit, BlockPos pos, SlabType primary) {
		return switch(axis) {
			case X -> isPositiveX(hit, pos, primary) ? Direction.EAST : Direction.WEST;
			case Y -> isPositiveY(hit, pos, primary) ? Direction.UP : Direction.DOWN;
			case Z -> isPositiveZ(hit, pos, primary) ? Direction.SOUTH : Direction.NORTH;
		};
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

	public static BlockState getSlabState(Block block, Direction direction) {
		SlabType type = direction.getDirection() == AxisDirection.POSITIVE ? SlabType.TOP : SlabType.BOTTOM;
		Axis axis = direction.getAxis();
		return block.getDefaultState().with(SlabBlock.TYPE, type).with(Properties.AXIS, axis);
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

	public static Direction getDirection(SlabType type, Axis axis) {
		return switch(type) {
			case TOP -> Direction.get(AxisDirection.POSITIVE, axis);
			case BOTTOM -> Direction.get(AxisDirection.NEGATIVE, axis);
			case DOUBLE -> throw new IllegalArgumentException("Slab type 'DOUBLE' is directionless!");
		};
	}

	private static double modOne(double value) {
		return value - Math.floor(value);
	}
}
