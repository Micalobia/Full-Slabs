package dev.micalobia.fullslabs.fabric.compat.mo_glass;

import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BeaconBeamBlock;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.wurstclient.glass.StainedGlassSlabBlock;
import net.wurstclient.glass.StainedGlassStairsBlock;

public class StainedGlassVerticalSlabBlock extends GlassVerticalSlabBlock implements BeaconBeamBlock {
    private final DyeColor color;

    public StainedGlassVerticalSlabBlock(StainedGlassSlabBlock block, Properties settings) {
        super(block, settings);
        this.color = block.getColor();
    }

    @Override
    public DyeColor getColor() {
        return this.color;
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState stateFrom, Direction direction) {
        var blockFrom = stateFrom.getBlock();
        if (blockFrom instanceof StainedGlassBlock stainedFrom)
            return stainedFrom.getColor() == this.getColor();
        if (stateFrom.is(this)) return isInvisibleToVerticalSlab(state, stateFrom, direction);
        if (stateFrom.is(this.parent)) return isInvisibleToGlassSlab(state, stateFrom, direction);
        if (blockFrom instanceof StainedGlassStairsBlock stainedFrom && stainedFrom.getColor() == this.getColor())
            return isInvisibleToGlassStairs(state, stateFrom, direction);
        return false;
    }
}
