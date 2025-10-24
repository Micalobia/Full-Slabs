package dev.micalobia.fullslabs.handlers;

import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

public final class BlindProjectileHitHandler implements MixedHandler {
    public static BlindProjectileHitHandler INSTANCE = new BlindProjectileHitHandler();

    private BlindProjectileHitHandler() {}

    @Override
    public void onProjectileHit(MixedContext.Sided context, World world, BlockHitResult hit, ProjectileEntity projectile) {
        var state = context.state();
        state.onProjectileHit(world, state, hit, projectile);
    }
}
