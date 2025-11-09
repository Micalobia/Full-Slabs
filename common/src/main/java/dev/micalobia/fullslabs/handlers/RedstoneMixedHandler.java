package dev.micalobia.fullslabs.handlers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;

public record RedstoneMixedHandler(int weak, int strong) implements MixedHandler {
    @Override
    public boolean isSignalSource(MixedContext.Sided context) {
        return true;
    }

    @Override
    public int getSignal(MixedContext.Sided context, BlockGetter world, BlockPos pos, Direction direction) {
        return this.weak;
    }

    @Override
    public int getDirectSignal(MixedContext.Sided context, BlockGetter world, BlockPos pos, Direction direction) {
        return this.strong;
    }
}
