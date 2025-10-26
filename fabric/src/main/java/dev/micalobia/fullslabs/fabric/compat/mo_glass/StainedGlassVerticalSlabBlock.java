package dev.micalobia.fullslabs.fabric.compat.mo_glass;

import net.minecraft.block.BlockState;
import net.minecraft.block.Stainable;
import net.minecraft.block.StainedGlassBlock;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.Direction;
import net.wurstclient.glass.StainedGlassSlabBlock;
import net.wurstclient.glass.StainedGlassStairsBlock;

public class StainedGlassVerticalSlabBlock extends GlassVerticalSlabBlock implements Stainable {
    private final DyeColor color;

    public StainedGlassVerticalSlabBlock(StainedGlassSlabBlock block, Settings settings) {
        super(block, settings);
        this.color = block.getColor();
    }

    @Override
    public DyeColor getColor() {
        return this.color;
    }

    @Override
    protected boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
        var blockFrom = stateFrom.getBlock();
        if (blockFrom instanceof StainedGlassBlock stainedFrom)
            return stainedFrom.getColor() == this.getColor();
        if (stateFrom.isOf(this)) return isInvisibleToVerticalSlab(state, stateFrom, direction);
        if (stateFrom.isOf(this.parent)) return isInvisibleToGlassSlab(state, stateFrom, direction);
        if (blockFrom instanceof StainedGlassStairsBlock stainedFrom && stainedFrom.getColor() == this.getColor())
            return isInvisibleToGlassStairs(state, stateFrom, direction);
        return false;
    }
}
