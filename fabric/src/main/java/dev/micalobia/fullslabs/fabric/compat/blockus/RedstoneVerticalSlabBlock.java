package dev.micalobia.fullslabs.fabric.compat.blockus;

import dev.micalobia.fullslabs.block.VerticalSlabBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

public class RedstoneVerticalSlabBlock extends VerticalSlabBlock {
    public RedstoneVerticalSlabBlock(SlabBlock block, Settings settings) {
        super(block, settings);
    }

    @Override
    protected boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @Override
    protected int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return 15;
    }
}
