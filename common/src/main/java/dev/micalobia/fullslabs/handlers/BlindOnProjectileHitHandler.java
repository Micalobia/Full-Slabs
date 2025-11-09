package dev.micalobia.fullslabs.handlers;

import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public final class BlindOnProjectileHitHandler implements MixedHandler {
    public static BlindOnProjectileHitHandler INSTANCE = new BlindOnProjectileHitHandler();

    private BlindOnProjectileHitHandler() {}

    @Override
    public void onProjectileHit(MixedContext.Sided context, Level world, BlockHitResult hit, Projectile projectile) {
        var state = context.state();
        state.onProjectileHit(world, state, hit, projectile);
    }
}
