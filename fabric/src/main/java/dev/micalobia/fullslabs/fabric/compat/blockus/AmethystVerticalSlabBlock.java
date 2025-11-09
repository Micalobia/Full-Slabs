package dev.micalobia.fullslabs.fabric.compat.blockus;

import dev.micalobia.fullslabs.block.VerticalSlabBlock;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class AmethystVerticalSlabBlock extends VerticalSlabBlock {
    public AmethystVerticalSlabBlock(SlabBlock block, Properties settings) {
        super(block, settings);
    }

    @Override
    protected void onProjectileHit(Level world, BlockState state, BlockHitResult hit, Projectile projectile) {
        if (!world.isClientSide()) {
            var pos = hit.getBlockPos();
            world.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0F, 0.5F + world.random.nextFloat() * 1.2F);
        }
    }
}
