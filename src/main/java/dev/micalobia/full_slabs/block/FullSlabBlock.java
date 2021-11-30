package dev.micalobia.full_slabs.block;

import com.mojang.datafixers.util.Pair;
import dev.micalobia.full_slabs.block.entity.FullSlabBlockEntity;
import dev.micalobia.full_slabs.util.Utility;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

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
		BlockState newState = SlabBlockUtility.getSlabState(slabs, axis, hit, pos);
		return newState.getBlock().getPickStack(world, pos, newState);
	}

	@Override
	protected void appendProperties(Builder<Block, BlockState> builder) {
		builder.add(AXIS, LIGHT);
	}
}
