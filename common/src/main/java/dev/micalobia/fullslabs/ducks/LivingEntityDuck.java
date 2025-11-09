package dev.micalobia.fullslabs.ducks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public interface LivingEntityDuck {
    BlockState fullslabs$getMixedLandingState(BlockState state, BlockPos landedPosition);
}
