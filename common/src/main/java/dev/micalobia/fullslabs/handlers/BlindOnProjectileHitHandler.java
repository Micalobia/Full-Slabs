package dev.micalobia.fullslabs.handlers;

import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class BlindOnProjectileHitHandler implements MixedHandler {
    public static final BlindOnProjectileHitHandler INSTANCE = new BlindOnProjectileHitHandler();

    private BlindOnProjectileHitHandler() {}

    @Override
    public void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        state.onProjectileHit(level, state, hit, projectile);
    }
}
