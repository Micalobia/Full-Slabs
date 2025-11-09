package dev.micalobia.fullslabs.fabric.compat.blockus;

import dev.micalobia.fullslabs.block.VerticalSlabBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;

public class RedstoneVerticalSlabBlock extends VerticalSlabBlock {
    public RedstoneVerticalSlabBlock(SlabBlock block, Properties settings) {
        super(block, settings);
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
        return 15;
    }
}
