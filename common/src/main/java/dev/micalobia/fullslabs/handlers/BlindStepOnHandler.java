package dev.micalobia.fullslabs.handlers;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public final class BlindStepOnHandler implements MixedHandler {
    public static BlindStepOnHandler INSTANCE = new BlindStepOnHandler();

    private BlindStepOnHandler() {}

    @Override
    public void stepOn(MixedContext.Sided context, Level world, BlockPos pos, Entity entity) {
        context.block().stepOn(world, pos, context.state(), entity);
    }
}
