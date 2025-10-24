package dev.micalobia.fullslabs.fabric.compat.blockus;

import dev.micalobia.fullslabs.block.VerticalSlabBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

public class AmethystVerticalSlabBlock extends VerticalSlabBlock {
    public AmethystVerticalSlabBlock(SlabBlock block, Settings settings) {
        super(block, settings);
    }

    @Override
    protected void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
        if (!world.isClient()) {
            var pos = hit.getBlockPos();
            world.playSound(null, pos, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.BLOCKS, 1.0F, 0.5F + world.random.nextFloat() * 1.2F);
        }
    }
}
