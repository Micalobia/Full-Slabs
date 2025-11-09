package dev.micalobia.fullslabs.fabric.compat.mo_glass;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.wurstclient.glass.MoGlassBlocks;

public class TintedGlassVerticalSlabBlock extends GlassVerticalSlabBlock {
    public TintedGlassVerticalSlabBlock(SlabBlock block, Properties settings) {
        super(block, settings);
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState stateFrom, Direction direction) {
        if (stateFrom.is(Blocks.TINTED_GLASS)) return true;
        if (stateFrom.is(this)) return isInvisibleToVerticalSlab(state, stateFrom, direction);
        if (stateFrom.is(this.parent)) return isInvisibleToGlassSlab(state, stateFrom, direction);
        if (stateFrom.is(MoGlassBlocks.TINTED_GLASS_STAIRS))
            return isInvisibleToGlassStairs(state, stateFrom, direction);
        return false;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return false;
    }

    @Override
    protected int getLightBlock(BlockState state) {
        return state.getValue(TYPE) == VerticalType.FULL ? 15 : 0;
    }
}
