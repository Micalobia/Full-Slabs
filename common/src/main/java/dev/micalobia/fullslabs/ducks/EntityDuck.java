package dev.micalobia.fullslabs.ducks;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public interface EntityDuck {
    BlockState fullslabs$tryGetMixedState(BlockState state, BlockPos pos);
}
