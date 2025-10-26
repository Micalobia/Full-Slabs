package dev.micalobia.fullslabs.fabric.compat.mo_glass;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.util.math.Direction;
import net.wurstclient.glass.MoGlassBlocks;

public class TintedGlassVerticalSlabBlock extends GlassVerticalSlabBlock {
    public TintedGlassVerticalSlabBlock(SlabBlock block, Settings settings) {
        super(block, settings);
    }

    @Override
    protected boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
        if (stateFrom.isOf(Blocks.TINTED_GLASS)) return true;
        if (stateFrom.isOf(this)) return isInvisibleToVerticalSlab(state, stateFrom, direction);
        if (stateFrom.isOf(this.parent)) return isInvisibleToGlassSlab(state, stateFrom, direction);
        if (stateFrom.isOf(MoGlassBlocks.TINTED_GLASS_STAIRS))
            return isInvisibleToGlassStairs(state, stateFrom, direction);
        return false;
    }

    @Override
    protected boolean isTransparent(BlockState state) {
        return false;
    }

    @Override
    protected int getOpacity(BlockState state) {
        return state.get(TYPE) == VerticalType.FULL ? 15 : 0;
    }
}
