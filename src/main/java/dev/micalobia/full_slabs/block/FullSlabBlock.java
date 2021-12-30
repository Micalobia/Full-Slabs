package dev.micalobia.full_slabs.block;

import dev.micalobia.full_slabs.block.entity.FullSlabBlockEntity;
import dev.micalobia.full_slabs.util.Utility;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

import java.util.Objects;

public class FullSlabBlock extends Block implements BlockEntityProvider {
	public final static EnumProperty<Axis> AXIS;
	public final static IntProperty LIGHT;

	static {
		AXIS = Properties.AXIS;
		LIGHT = IntProperty.of("light", 0, 15);
	}

	public FullSlabBlock(Settings settings) {
		super(settings);
		setDefaultState(getDefaultState().with(AXIS, Axis.Y).with(LIGHT, 0));
	}

	public static int stateToLuminance(BlockState state) {
		return state.get(LIGHT);
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
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return VoxelShapes.fullCube();
	}

	@Override
	public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
		FullSlabBlockEntity entity = (FullSlabBlockEntity) world.getBlockEntity(pos);
		Objects.requireNonNull(entity);
		Vec3d hit = Utility.crosshair(player).getPos();
		BlockState hitState = entity.getSlabState(hit);
		return hitState.getBlock().calcBlockBreakingDelta(hitState, player, world, pos);
	}

	@Override
	public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
		FullSlabBlockEntity entity = (FullSlabBlockEntity) world.getBlockEntity(pos);
		if(entity == null) return ItemStack.EMPTY;
		HitResult hitResult = MinecraftClient.getInstance().crosshairTarget;
		BlockState pickState;
		if(hitResult == null || hitResult.getType() != Type.BLOCK) pickState = entity.getPositiveSlabState();
		else pickState = entity.getSlabState(hitResult.getPos());
		return pickState.getBlock().getPickStack(world, pos, pickState);
	}

	@Override
	protected void appendProperties(Builder<Block, BlockState> builder) {
		builder.add(AXIS, LIGHT);
	}
}
