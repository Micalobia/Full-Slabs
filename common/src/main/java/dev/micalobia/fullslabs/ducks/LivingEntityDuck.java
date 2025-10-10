package dev.micalobia.fullslabs.ducks;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public interface LivingEntityDuck {
    BlockState fullslabs$getMixedLandingState(BlockState state, BlockPos landedPosition);
}
