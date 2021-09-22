package dev.micalobia.full_slabs.block;

import com.mojang.datafixers.util.Pair;
import dev.micalobia.full_slabs.block.entity.FullSlabBlockEntity;
import dev.micalobia.full_slabs.util.Utility;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager.Builder;
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

public class FullSlabBlock extends TransparentBlock implements BlockEntityProvider {
	public final static EnumProperty<Axis> AXIS;

	static {
		AXIS = Properties.AXIS;
	}

	public FullSlabBlock(Settings settings) {
		super(settings);
		setDefaultState(getDefaultState().with(AXIS, Axis.Y));
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new FullSlabBlockEntity(pos, state);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	public VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
		return VoxelShapes.fullCube();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		Axis axis = state.get(AXIS);
		HitResult hitResult = MinecraftClient.getInstance().crosshairTarget;
		if(hitResult == null) return VoxelShapes.fullCube();
		Direction direction = Utility.getDirection(axis, hitResult.getPos(), pos);
		return switch(direction) {
			case UP -> Utility.TOP_OUTLINE_SHAPE;
			case DOWN -> Utility.BOTTOM_OUTLINE_SHAPE;
			case NORTH -> Utility.NORTH_OUTLINE_SHAPE;
			case EAST -> Utility.EAST_OUTLINE_SHAPE;
			case SOUTH -> Utility.SOUTH_OUTLINE_SHAPE;
			case WEST -> Utility.WEST_OUTLINE_SHAPE;
		};
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return VoxelShapes.fullCube();
	}

	@Override
	public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
		FullSlabBlockEntity entity = (FullSlabBlockEntity) world.getBlockEntity(pos);
		assert entity != null;
		Vec3d hit = Utility.crosshair(player).getPos();
		BlockState hitState = entity.getSlabState(hit);
		return hitState.getBlock().calcBlockBreakingDelta(hitState, player, world, pos);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
		RenderAttachedBlockView view = (RenderAttachedBlockView) world;
		Pair<Block, Block> slabs = (Pair<Block, Block>) view.getBlockEntityRenderAttachment(pos);
		MinecraftClient mc = MinecraftClient.getInstance();
		assert mc.crosshairTarget != null;
		Vec3d hit = mc.crosshairTarget.getPos();
		Axis axis = state.get(AXIS);
		BlockState newState = Utility.getSlabState(slabs, axis, hit, pos);
		return newState.getBlock().getPickStack(world, pos, newState);
	}

	@Override
	protected void appendProperties(Builder<Block, BlockState> builder) {
		builder.add(AXIS);
	}
}
