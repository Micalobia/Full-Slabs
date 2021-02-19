package dev.micalobia.full_slabs.block;

import dev.micalobia.full_slabs.block.enums.SlabState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;

public class VerticalSlabBlock extends Block implements Waterloggable {

	public static final EnumProperty<Axis> AXIS;
	public static final EnumProperty<SlabState> STATE;
	public static final BooleanProperty WATERLOGGED;

	protected static final VoxelShape NORTH_SHAPE;
	protected static final VoxelShape EAST_SHAPE;
	protected static final VoxelShape SOUTH_SHAPE;
	protected static final VoxelShape WEST_SHAPE;

	static {
		AXIS = net.minecraft.state.property.Properties.HORIZONTAL_AXIS;
		STATE = dev.micalobia.full_slabs.state.property.Properties.SLAB_STATE;
		WATERLOGGED = net.minecraft.state.property.Properties.WATERLOGGED;

		NORTH_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 8.0D);
		SOUTH_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 8.0D, 16.0D, 16.0D, 16.0D);
		WEST_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 8.0D, 16.0D, 16.0D);
		EAST_SHAPE = Block.createCuboidShape(8.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
	}

	public final boolean tiltable;

	public VerticalSlabBlock(Settings settings, boolean tiltable) {
		super(settings);
		this.tiltable = tiltable;
		setDefaultState(getDefaultState().with(AXIS, Axis.X).with(STATE, SlabState.NEGATIVE).with(WATERLOGGED, false));
	}

	protected void appendProperties(Builder<Block, BlockState> builder) {
		builder.add(AXIS, STATE, WATERLOGGED);
	}

	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		SlabState slabState = state.get(STATE);
		if (slabState == SlabState.DOUBLE) return VoxelShapes.fullCube();
		Axis axis = state.get(AXIS);
		if (slabState == SlabState.POSITIVE) {
			if (axis == Axis.X) return EAST_SHAPE;
			else return SOUTH_SHAPE;
		} else {
			if (axis == Axis.X) return WEST_SHAPE;
			else return NORTH_SHAPE;
		}
	}

	public FluidState getFluidState(BlockState state) {
		return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
	}

	public boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {
		return state.get(STATE) != SlabState.DOUBLE && Waterloggable.super.tryFillWithFluid(world, pos, state, fluidState);
	}

	public boolean canFillWithFluid(BlockView world, BlockPos pos, BlockState state, Fluid fluid) {
		return state.get(STATE) != SlabState.DOUBLE && Waterloggable.super.canFillWithFluid(world, pos, state, fluid);
	}

	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world, BlockPos pos, BlockPos posFrom) {
		if (state.get(WATERLOGGED))
			world.getFluidTickScheduler().schedule(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
		return super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom);
	}
}
