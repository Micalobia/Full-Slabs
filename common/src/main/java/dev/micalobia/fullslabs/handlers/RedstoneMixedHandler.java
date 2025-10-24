package dev.micalobia.fullslabs.handlers;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

public record RedstoneMixedHandler(int weak, int strong) implements MixedHandler {
    @Override
    public boolean emitsRedstonePower(MixedContext.Sided context) {
        return true;
    }

    @Override
    public int getWeakRedstonePower(MixedContext.Sided context, BlockView world, BlockPos pos, Direction direction) {
        return this.weak;
    }

    @Override
    public int getStrongRedstonePower(MixedContext.Sided context, BlockView world, BlockPos pos, Direction direction) {
        return this.strong;
    }
}
