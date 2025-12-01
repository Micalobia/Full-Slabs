package dev.micalobia.fullslabs.handlers;

import dev.micalobia.fullslabs.util.SlabContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public final class BlindStepOnHandler implements MixedHandler {
    public static final BlindStepOnHandler INSTANCE = new BlindStepOnHandler();

    private BlindStepOnHandler() {}

    @Override
    public void stepOn(SlabContext context, Level world, BlockPos pos, Entity entity) {
        context.mainBlock().stepOn(world, pos, context.mainState(), entity);
    }
}
