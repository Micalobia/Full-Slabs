package dev.micalobia.full_slabs.block;

import com.mojang.datafixers.util.Pair;
import dev.micalobia.full_slabs.block.entity.FullSlabBlockEntity;
import dev.micalobia.full_slabs.util.Helper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class FullSlabBlock extends BlockWithEntity {
	public final static EnumProperty<Axis> AXIS;

	public static final VoxelShape NORTH_OUTLINE_SHAPE;
	public static final VoxelShape EAST_OUTLINE_SHAPE;
	public static final VoxelShape SOUTH_OUTLINE_SHAPE;
	public static final VoxelShape WEST_OUTLINE_SHAPE;
	public static final VoxelShape TOP_OUTLINE_SHAPE;
	public static final VoxelShape BOTTOM_OUTLINE_SHAPE;

	public static final VoxelShape NORTH_COLLISION_SHAPE;
	public static final VoxelShape EAST_COLLISION_SHAPE;
	public static final VoxelShape SOUTH_COLLISION_SHAPE;
	public static final VoxelShape WEST_COLLISION_SHAPE;
	public static final VoxelShape TOP_COLLISION_SHAPE;
	public static final VoxelShape BOTTOM_COLLISION_SHAPE;

	static {
		AXIS = Properties.AXIS;
		NORTH_OUTLINE_SHAPE = VerticalSlabBlock.NORTH_OUTLINE_SHAPE;
		EAST_OUTLINE_SHAPE = VerticalSlabBlock.EAST_OUTLINE_SHAPE;
		SOUTH_OUTLINE_SHAPE = VerticalSlabBlock.SOUTH_OUTLINE_SHAPE;
		WEST_OUTLINE_SHAPE = VerticalSlabBlock.WEST_OUTLINE_SHAPE;
		TOP_OUTLINE_SHAPE = SlabBlockConstants.TOP_OUTLINE_SHAPE;
		BOTTOM_OUTLINE_SHAPE = SlabBlockConstants.BOTTOM_OUTLINE_SHAPE;

		NORTH_COLLISION_SHAPE = VerticalSlabBlock.NORTH_COLLISION_SHAPE;
		EAST_COLLISION_SHAPE = VerticalSlabBlock.EAST_COLLISION_SHAPE;
		SOUTH_COLLISION_SHAPE = VerticalSlabBlock.SOUTH_COLLISION_SHAPE;
		WEST_COLLISION_SHAPE = VerticalSlabBlock.WEST_COLLISION_SHAPE;
		TOP_COLLISION_SHAPE = SlabBlock.TOP_SHAPE;
		BOTTOM_COLLISION_SHAPE = SlabBlock.BOTTOM_SHAPE;
	}

	protected FullSlabBlock(Settings settings) {
		super(settings);
		setDefaultState(getDefaultState().with(AXIS, Axis.Y));
	}

	private static Vec3d crosshair(PlayerEntity player) {
		return player.raycast(player.isCreative() ? 4.5f : 3.0f, 0f, false).getPos();
	}

	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	public BlockEntity createBlockEntity(BlockView blockView) {
		return new FullSlabBlockEntity();
	}

	public VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
		return VoxelShapes.fullCube();
	}

	@Environment(EnvType.CLIENT)
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		Axis axis = state.get(AXIS);
		HitResult hitResult = MinecraftClient.getInstance().crosshairTarget;
		if(hitResult == null) return VoxelShapes.fullCube();
		Vec3d hit = hitResult.getPos();
		boolean positive = Helper.isPositive(hit, pos, axis);
		switch(axis) {
			case X:
				return positive ? EAST_OUTLINE_SHAPE : WEST_OUTLINE_SHAPE;
			case Y:
				return positive ? TOP_OUTLINE_SHAPE : BOTTOM_OUTLINE_SHAPE;
			case Z:
				return positive ? SOUTH_OUTLINE_SHAPE : NORTH_OUTLINE_SHAPE;
		}
		return VoxelShapes.fullCube(); // Should never reach here, but it's a good default
	}

	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return VoxelShapes.fullCube(); // Should never reach here, but it's a good default
	}

	public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
		FullSlabBlockEntity entity = (FullSlabBlockEntity) world.getBlockEntity(pos);
		Vec3d hit = crosshair(player);
		Axis axis = state.get(AXIS);
		boolean hitPositive = Helper.isPositive(hit, pos, axis);
		Block hitSlab = hitPositive ? entity.getPositiveSlab() : entity.getNegativeSlab();
		BlockState hitState = Helper.getState(hitSlab, axis, hitPositive);
		return hitSlab.calcBlockBreakingDelta(hitState, player, world, pos);
	}

	public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
		RenderAttachedBlockView view = (RenderAttachedBlockView) world;
		Pair<Block, Block> slabs = (Pair<Block, Block>) view.getBlockEntityRenderAttachment(pos);
		Vec3d hit = MinecraftClient.getInstance().crosshairTarget.getPos();
		Axis axis = state.get(AXIS);
		boolean isPositive = Helper.isPositive(hit, pos, axis);
		BlockState slab = Helper.getState(isPositive ? slabs.getFirst() : slabs.getSecond(), axis, isPositive);
		return slab.getBlock().getPickStack(world, pos, slab);
	}

	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(AXIS);
	}
}
