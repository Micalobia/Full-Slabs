package dev.micalobia.fullslabs.traits;

import dev.micalobia.fullslabs.SlabRegistryBridge;
import dev.micalobia.fullslabs.block.VerticalSlabBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Oxidizable;
import net.minecraft.block.OxidizableSlabBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.item.HoneycombItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public final class OxidizableTrait implements SlabTrait {
    private final SlabBlock parent;
    private static final Requirements REQUIREMENTS = Requirements.EMPTY.withRandomTicks();

    public OxidizableTrait(OxidizableSlabBlock slab) {
        this.parent = slab;
    }

    @Override
    public SlabBlock parent() {
        return this.parent;
    }

    @Override
    public Requirements requirements() {
        return REQUIREMENTS;
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        var oxidizable = (Oxidizable) this.parent;
        oxidizable.tryDegrade(state, world, pos, random).ifPresent(s -> world.setBlockState(pos, s));
    }

    @Override
    public void postInit() {
        var original = VerticalSlabBlock.getVertical(this.parent);
        var oxidized = Oxidizable.getIncreasedOxidationBlock(this.parent).map(block -> VerticalSlabBlock.getVertical((SlabBlock) block));
        var waxed = HoneycombItem.getWaxedState(this.parent.getDefaultState()).map(state -> VerticalSlabBlock.getVertical((SlabBlock) state.getBlock()));
        var waxed_oxidized = Oxidizable.getIncreasedOxidationBlock(this.parent)
                .flatMap(block -> HoneycombItem.getWaxedState(block.getDefaultState()))
                .map(state -> VerticalSlabBlock.getVertical((SlabBlock) state.getBlock()));
        oxidized.ifPresent(oxBlock -> {
            SlabRegistryBridge.registerOxidizableBlockPair(original, oxBlock);
            // This is so that the max oxidization gets registered correctly, since it doesn't get the trait naturally
            waxed_oxidized.ifPresent(waxBlock -> SlabRegistryBridge.registerWaxableBlockPair(oxBlock, waxBlock));
        });
        waxed.ifPresent(block -> SlabRegistryBridge.registerWaxableBlockPair(original, block));
    }
}
