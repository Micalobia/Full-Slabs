package dev.micalobia.fullslabs.traits;

import net.minecraft.block.BlockState;
import net.minecraft.block.Oxidizable;
import net.minecraft.block.OxidizableSlabBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public final class OxidizableTrait implements SlabTrait {
    private final SlabBlock parent;

    public OxidizableTrait(OxidizableSlabBlock slab) {
        this.parent = slab;
    }

    @Override
    public SlabBlock parent() {
        return this.parent;
    }

    @Override
    public Requirements requirements() {
        return new Requirements(true);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        var oxidizable = (Oxidizable) this.parent;
        oxidizable.tryDegrade(state, world, pos, random).ifPresent(s -> world.setBlockState(pos, s));
    }
}
