package dev.micalobia.fullslabs.handlers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public record RedstoneMixedHandler(int weak, int strong) implements MixedHandler {
    @Override
    public boolean isSignalSource(BlockState state, BlockGetter level, BlockPos pos) {return true;}

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {return this.weak;}

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {return this.strong;}
}
