package dev.micalobia.fullslabs.handlers;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class BlindStepOnHandler implements MixedHandler {
    public static final BlindStepOnHandler INSTANCE = new BlindStepOnHandler();

    private BlindStepOnHandler() {}

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        state.getBlock().stepOn(level, pos, state, entity);
    }
}
