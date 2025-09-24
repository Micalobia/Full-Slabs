package dev.micalobia.fullslabs.traits;

import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

public class SimpleRedstoneTrait implements SlabTrait {
    private final int weak;
    private final int strong;
    private final SlabBlock parent;
    private static final Requirements REQUIREMENTS = Requirements.EMPTY.withRedstonePower();


    public SimpleRedstoneTrait(SlabBlock parent, int weak, int strong) {
        this.parent = parent;
        this.weak = weak;
        this.strong = strong;
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
    public int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return strong;
    }

    @Override
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return weak;
    }
}
