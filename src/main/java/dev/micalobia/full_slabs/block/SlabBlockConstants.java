package dev.micalobia.full_slabs.block;

import net.minecraft.block.Block;
import net.minecraft.util.shape.VoxelShape;

public abstract class SlabBlockConstants extends Block {
	public static final VoxelShape TOP_OUTLINE_SHAPE;
	public static final VoxelShape BOTTOM_OUTLINE_SHAPE;

	static {
		BOTTOM_OUTLINE_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 8.05D, 16.0D);
		TOP_OUTLINE_SHAPE = Block.createCuboidShape(0.0D, 7.95D, 0.0D, 16.0D, 16.0D, 16.0D);
	}

	public SlabBlockConstants(Settings settings) {
		super(settings);
		throw new RuntimeException("This isn't supposed to be initilized!");
	}
}
