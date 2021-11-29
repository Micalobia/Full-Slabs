package dev.micalobia.full_slabs.block;

import com.mojang.datafixers.util.Pair;
import dev.micalobia.full_slabs.block.entity.ExtraSlabBlockEntity;
import dev.micalobia.full_slabs.config.SlabExtra;
import dev.micalobia.full_slabs.util.Utility;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.SlabType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class ExtraSlabBlock extends Block implements BlockEntityProvider, Waterloggable {
	public static final EnumProperty<SlabType> TYPE;
	public static final EnumProperty<Axis> AXIS;
	public static final BooleanProperty WATERLOGGED;
	public static final IntProperty LIGHT;

	static {
		TYPE = EnumProperty.of("type", SlabType.class, SlabType.BOTTOM, SlabType.TOP);
		AXIS = Properties.AXIS;
		WATERLOGGED = Properties.WATERLOGGED;
		LIGHT = Properties.LEVEL_15;
	}

	public ExtraSlabBlock(Settings settings) {
		super(settings);
		setDefaultState(getDefaultState().with(TYPE, SlabType.BOTTOM).with(AXIS, Axis.Y).with(WATERLOGGED, false).with(LIGHT, 0));
	}

	public static Direction getDirection(BlockState state) {
		return SlabBlockUtility.getDirection(state.get(TYPE), state.get(AXIS));
	}

	public static int stateToLuminance(BlockState state) {
		return state.get(LIGHT);
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new ExtraSlabBlockEntity(pos, state);
	}

	@Override
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
		if(state.get(WATERLOGGED))
			world.getFluidTickScheduler().schedule(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
		return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
	}

	@Override
	public boolean hasSidedTransparency(BlockState state) {
		return true;
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		ExtraSlabBlockEntity entity = (ExtraSlabBlockEntity) world.getBlockEntity(pos);
		if(entity == null) return VoxelShapes.empty();
		return VoxelShapes.union(entity.getBaseOutlineShape(world, pos, context), entity.getExtraOutlineShape(world, pos, context));
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		ExtraSlabBlockEntity entity = (ExtraSlabBlockEntity) world.getBlockEntity(pos);
		if(entity == null) return VoxelShapes.empty();
		return VoxelShapes.union(entity.getBaseCollisionShape(world, pos, context), entity.getExtraCollisionShape(world, pos, context));
	}

	@Override
	public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
		ExtraSlabBlockEntity entity = (ExtraSlabBlockEntity) world.getBlockEntity(pos);
		assert entity != null;
		Vec3d hit = Utility.crosshair(player).getPos();
		BlockState hitState = entity.getState(hit);
		return hitState.getBlock().calcBlockBreakingDelta(hitState, player, world, pos);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
		RenderAttachedBlockView view = (RenderAttachedBlockView) world;
		Pair<Block, SlabExtra> pair = (Pair<Block, SlabExtra>) view.getBlockEntityRenderAttachment(pos);
		assert pair != null;
		MinecraftClient mc = MinecraftClient.getInstance();
		assert mc.crosshairTarget != null;
		Vec3d hit = mc.crosshairTarget.getPos();
		Axis axis = state.get(AXIS);
		SlabType type = state.get(TYPE);
		boolean positive = SlabBlockUtility.isPositive(axis, hit, pos, type);
		Direction direction = SlabBlockUtility.getDirection(type, axis);
		boolean isBase = positive == (type == SlabType.TOP);
		if(isBase) {
			Block base = pair.getFirst();
			return base.getPickStack(world, pos, SlabBlockUtility.getSlabState(base, direction));
		}
		SlabExtra extra = pair.getSecond();
		BlockState newState = extra.getState(direction);
		assert newState != null;
		Block block = newState.getBlock();
		return block.getPickStack(world, pos, newState);
	}

	@Override
	protected void appendProperties(Builder<Block, BlockState> builder) {
		builder.add(TYPE, AXIS, LIGHT, WATERLOGGED);
	}

	@Override
	public boolean canFillWithFluid(BlockView world, BlockPos pos, BlockState state, Fluid fluid) {
		ExtraSlabBlockEntity entity = (ExtraSlabBlockEntity) world.getBlockEntity(pos);
		assert entity != null;
		if(entity.waterloggable()) return Waterloggable.super.canFillWithFluid(world, pos, state, fluid);
		return false;
	}

	@Override
	public boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {
		ExtraSlabBlockEntity entity = (ExtraSlabBlockEntity) world.getBlockEntity(pos);
		assert entity != null;
		if(entity.waterloggable()) return Waterloggable.super.tryFillWithFluid(world, pos, state, fluidState);
		return false;
	}
}
