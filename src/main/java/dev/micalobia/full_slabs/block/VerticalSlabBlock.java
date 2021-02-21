package dev.micalobia.full_slabs.block;

import dev.micalobia.full_slabs.block.enums.SlabState;
import dev.micalobia.full_slabs.util.Helper;
import dev.micalobia.full_slabs.util.LinkedSlabs;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class VerticalSlabBlock extends Block implements Waterloggable, ISlabBlock {

	public static final EnumProperty<Axis> AXIS;
	public static final EnumProperty<SlabState> STATE;
	public static final BooleanProperty WATERLOGGED;

	protected static final VoxelShape NORTH_OUTLINE_SHAPE;
	protected static final VoxelShape EAST_OUTLINE_SHAPE;
	protected static final VoxelShape SOUTH_OUTLINE_SHAPE;
	protected static final VoxelShape WEST_OUTLINE_SHAPE;
	protected static final VoxelShape NORTH_COLLISION_SHAPE;
	protected static final VoxelShape EAST_COLLISION_SHAPE;
	protected static final VoxelShape SOUTH_COLLISION_SHAPE;
	protected static final VoxelShape WEST_COLLISION_SHAPE;

	static {
		AXIS = net.minecraft.state.property.Properties.HORIZONTAL_AXIS;
		STATE = dev.micalobia.full_slabs.state.property.Properties.SLAB_STATE;
		WATERLOGGED = net.minecraft.state.property.Properties.WATERLOGGED;

		NORTH_OUTLINE_SHAPE = Block.createCuboidShape(0d, 0d, 0d, 16d, 16d, 8.05d);
		SOUTH_OUTLINE_SHAPE = Block.createCuboidShape(0d, 0d, 7.95d, 16d, 16d, 16d);
		WEST_OUTLINE_SHAPE = Block.createCuboidShape(0d, 0d, 0d, 8.05d, 16d, 16d);
		EAST_OUTLINE_SHAPE = Block.createCuboidShape(7.95d, 0d, 0d, 16d, 16d, 16d);

		NORTH_COLLISION_SHAPE = Block.createCuboidShape(0d, 0d, 0d, 16d, 16d, 8d);
		SOUTH_COLLISION_SHAPE = Block.createCuboidShape(0d, 0d, 8d, 16d, 16d, 16d);
		WEST_COLLISION_SHAPE = Block.createCuboidShape(0d, 0d, 0d, 8d, 16d, 16d);
		EAST_COLLISION_SHAPE = Block.createCuboidShape(8d, 0d, 0d, 16d, 16d, 16d);
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

		Axis axis = state.get(AXIS);
		if (slabState == SlabState.DOUBLE) {
			HitResult hit = MinecraftClient.getInstance().crosshairTarget;
			if (hit == null || hit.getType() != Type.BLOCK) return VoxelShapes.fullCube();
			boolean positive = isInside(state.with(STATE, SlabState.NEGATIVE), hit.getPos(), pos);
			int cas = positive ? 1 : 0;
			cas |= (axis == Axis.X ? 2 : 0);

			switch(cas) {
				case 0: return NORTH_OUTLINE_SHAPE;
				case 1: return SOUTH_OUTLINE_SHAPE;
				case 2: return WEST_OUTLINE_SHAPE;
				default: return EAST_OUTLINE_SHAPE;
			}
		}
		if (slabState == SlabState.POSITIVE) {
			if (axis == Axis.X) return EAST_OUTLINE_SHAPE;
			else return SOUTH_OUTLINE_SHAPE;
		} else {
			if (axis == Axis.X) return WEST_OUTLINE_SHAPE;
			else return NORTH_OUTLINE_SHAPE;
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

	public boolean canReplace(BlockState state, ItemPlacementContext ctx) {
		if (!(Helper.fetchBlock(ctx.getStack().getItem()) instanceof SlabBlock)) return false;
		SlabState slabState = state.get(STATE);
		if (slabState == SlabState.DOUBLE) return false;
		if (ctx.canReplaceExisting()) return isInside(state, ctx.getSide(), ctx.getHitPos(), ctx.getBlockPos());
		return true;
	}

	public @Nullable Direction direction(BlockState state) {
		SlabState slabState = state.get(STATE);
		Axis axis = state.get(AXIS);
		return slabState.direction(axis);
	}

	public Axis axis(BlockState state) {
		return state.get(AXIS);
	}

	public boolean isInside(BlockState state, Vec3d hit, BlockPos pos) {
		SlabState slabState = state.get(STATE);
		if (slabState == SlabState.DOUBLE) return false;
		boolean isPositive;
		Axis axis = state.get(AXIS);
		if (axis == Axis.X) isPositive = hit.getX() - pos.getX() > 0.5;
		else isPositive = hit.getZ() - pos.getZ() > 0.5;
		return isPositive == (slabState == SlabState.NEGATIVE);
	}

	@Override
	public VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
		SlabState slabState = state.get(STATE);
		Axis axis = state.get(AXIS);
		switch(slabState) {
			case POSITIVE: return axis == Axis.X ? EAST_OUTLINE_SHAPE : SOUTH_OUTLINE_SHAPE;
			case NEGATIVE: return axis == Axis.X ? WEST_OUTLINE_SHAPE : NORTH_OUTLINE_SHAPE;
			default: return VoxelShapes.fullCube();
		}
	}

	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		SlabState slabState = state.get(STATE);
		Axis axis = state.get(AXIS);
		switch(slabState) {
			case POSITIVE: return axis == Axis.X ? EAST_COLLISION_SHAPE : SOUTH_COLLISION_SHAPE;
			case NEGATIVE: return axis == Axis.X ? WEST_COLLISION_SHAPE : NORTH_COLLISION_SHAPE;
			default: return VoxelShapes.fullCube();
		}
	}

	@Override
	public List<ItemStack> getDroppedStacks(BlockState state, LootContext.Builder builder) {
		Block base = LinkedSlabs.horizontal(state.getBlock());
		List<ItemStack> ret = new ArrayList<>();
		ret.add(new ItemStack(base, state.get(STATE) == SlabState.DOUBLE ? 2 : 1));
		return ret;
	}
}
