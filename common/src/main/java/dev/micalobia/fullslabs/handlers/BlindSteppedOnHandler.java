package dev.micalobia.fullslabs.handlers;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class BlindSteppedOnHandler implements MixedHandler {
    public static BlindSteppedOnHandler INSTANCE = new BlindSteppedOnHandler();

    private BlindSteppedOnHandler() {}

    @Override
    public void onSteppedOn(MixedContext.Sided context, World world, BlockPos pos, Entity entity) {
        context.block().onSteppedOn(world, pos, context.state(), entity);
    }
}
