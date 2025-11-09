package dev.micalobia.fullslabs.ducks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public interface EntityDuck {
    BlockState fullslabs$tryGetMixedState(BlockState state, BlockPos pos);
}
