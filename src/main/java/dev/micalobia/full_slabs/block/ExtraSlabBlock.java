package dev.micalobia.full_slabs.block;

import dev.micalobia.full_slabs.block.entity.ExtraSlabBlockEntity;
import dev.micalobia.full_slabs.util.Utility;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.SlabType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
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

	@Environment(EnvType.CLIENT)
	public static Direction getHitDirection(BlockState state, BlockPos pos, BlockView world) {
		ExtraSlabBlockEntity entity = (ExtraSlabBlockEntity) world.getBlockEntity(pos);
		if(entity == null) throw new RuntimeException("Extra Slab Entity does not exist at " + pos.toShortString());
		HitResult hitResult = MinecraftClient.getInstance().crosshairTarget;
		if(hitResult == null) throw new RuntimeException("Hit result doesn't exist!");
		Vec3d hitPos = hitResult.getPos();
		Vec3d relPos = hitPos.subtract(pos.getX(), pos.getY(), pos.getZ());

		VoxelShape baseShape = entity.getBaseOutlineShape(world, pos, ShapeContext.absent());
		VoxelShape extraShape = entity.getExtraOutlineShape(world, pos, ShapeContext.absent());

		Axis axis = state.get(AXIS);
		SlabType type = state.get(TYPE);
		Direction dir = Utility.getDirection(type, axis);

		if(Utility.contains(extraShape, relPos)) return dir.getOpposite();
		if(Utility.contains(baseShape, relPos)) return dir;

		boolean retBase = Utility.isPositive(axis, hitPos, pos) == (type == SlabType.TOP);
		if(!retBase && Utility.borders(baseShape, relPos, axis)) return dir;
		return retBase ? dir : dir.getOpposite();
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
		VoxelShape base = entity.getBaseState().getRaycastShape(world, pos);
		VoxelShape extra = entity.getExtraState().getRaycastShape(world, pos);
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
