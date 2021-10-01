package dev.micalobia.full_slabs.block;

import dev.micalobia.full_slabs.block.entity.ExtraSlabBlockEntity;
import dev.micalobia.full_slabs.util.Utility;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.SlabType;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

public class ExtraSlabBlock extends Block implements BlockEntityProvider, Waterloggable {
	public static final EnumProperty<SlabType> TYPE;
	public static final EnumProperty<Axis> AXIS;
	public static final BooleanProperty WATERLOGGED;

	static {
		TYPE = EnumProperty.of("type", SlabType.class, SlabType.BOTTOM, SlabType.TOP);
		AXIS = Properties.AXIS;
		WATERLOGGED = Properties.WATERLOGGED;
	}

	public ExtraSlabBlock(Settings settings) {
		super(settings);
		setDefaultState(getDefaultState().with(TYPE, SlabType.BOTTOM).with(AXIS, Axis.Y).with(WATERLOGGED, false));
	}

	public static Direction getDirection(BlockState state) {
		return Utility.getDirection(state.get(TYPE), state.get(AXIS));
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new ExtraSlabBlockEntity(pos, state);
	}

	@Override
	protected void appendProperties(Builder<Block, BlockState> builder) {
		builder.add(TYPE, AXIS, WATERLOGGED);
	}

	@Override
	public boolean hasSidedTransparency(BlockState state) {
		return state.get(TYPE) != SlabType.DOUBLE;
	}

	@Override
	public VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
		ExtraSlabBlockEntity entity = (ExtraSlabBlockEntity) world.getBlockEntity(pos);
		if(entity == null) return VoxelShapes.empty();
		VoxelShape base = entity.getBaseRaycastShape(world, pos);
		VoxelShape extra = entity.getExtraRaycastShape(world, pos);
		return VoxelShapes.cuboid(VoxelShapes.union(base, extra).getBoundingBox());
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
}
