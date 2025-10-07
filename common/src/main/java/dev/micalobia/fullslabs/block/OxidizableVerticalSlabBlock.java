package dev.micalobia.fullslabs.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.Oxidizable;
import net.minecraft.block.OxidizableSlabBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public class OxidizableVerticalSlabBlock extends VerticalSlabBlock implements Oxidizable {
    private final OxidationLevel oxidationLevel;

    public OxidizableVerticalSlabBlock(OxidizableSlabBlock block, Settings settings) {
        super(block, settings);
        oxidationLevel = block.getDegradationLevel();
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        this.tickDegradation(state, world, pos, random);
    }

    @Override
    protected boolean hasRandomTicks(BlockState state) {
        return Oxidizable.getIncreasedOxidationBlock(this.parent).isPresent();
    }

    @Override
    public OxidationLevel getDegradationLevel() {
        return oxidationLevel;
    }
}
