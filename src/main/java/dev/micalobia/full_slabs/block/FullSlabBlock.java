package dev.micalobia.full_slabs.block;

import dev.micalobia.full_slabs.block.entity.FullSlabBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

public class FullSlabBlock extends BlockWithEntity {
	public final static EnumProperty<Axis> AXIS;

	public static final VoxelShape NORTH_SHAPE;
	public static final VoxelShape EAST_SHAPE;
	public static final VoxelShape SOUTH_SHAPE;
	public static final VoxelShape WEST_SHAPE;
	public static final VoxelShape TOP_SHAPE;
	public static final VoxelShape BOTTOM_SHAPE;

	static {
		AXIS = Properties.AXIS;
		NORTH_SHAPE = VerticalSlabBlock.NORTH_OUTLINE_SHAPE;
		EAST_SHAPE = VerticalSlabBlock.EAST_OUTLINE_SHAPE;
		SOUTH_SHAPE = VerticalSlabBlock.SOUTH_OUTLINE_SHAPE;
		WEST_SHAPE = VerticalSlabBlock.WEST_OUTLINE_SHAPE;
		TOP_SHAPE = SlabBlock.TOP_SHAPE;
		BOTTOM_SHAPE = SlabBlock.BOTTOM_SHAPE;
	}

	protected FullSlabBlock(Settings settings) {
		super(settings);
		setDefaultState(getDefaultState().with(AXIS, Axis.Y));
	}

	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	public BlockEntity createBlockEntity(BlockView blockView) {
		return new FullSlabBlockEntity();
	}

	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(AXIS);
	}
}
