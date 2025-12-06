package dev.micalobia.fullslabs.block;

import dev.micalobia.fullslabs.util.MixedType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public interface SlabLike {
    /**
     * Returns a single slab in the specified direcition based on an existing one
     *
     * @param isTowards Whether to get the half towards the facing direcition
     * @return The half specified by isTowards. Can return {@code Blocks.AIR} and {@code Blocks.WATER}
     */
    BlockState getHalf(BlockState state, BlockGetter level, BlockPos pos, boolean isTowards);

    boolean isVanilla();

    boolean isVertical();

    boolean isMixed();

    boolean supportsMixing();

    boolean hasVertical();

    boolean isDouble(BlockState state);

    boolean isSingle(BlockState state);

    MixedType getType(BlockState state);

    static boolean isDoubleSlab(BlockState state) {
        return state.getBlock() instanceof SlabLike slab && slab.isDouble(state);
    }

    static boolean isSingleSlab(BlockState state) {
        return state.getBlock() instanceof SlabLike slab && slab.isSingle(state);
    }
}
