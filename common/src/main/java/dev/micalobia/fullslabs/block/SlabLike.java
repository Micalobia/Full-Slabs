package dev.micalobia.fullslabs.block;

import dev.micalobia.fullslabs.SlabRegistry;
import dev.micalobia.fullslabs.block.entity.MixedSlabBlockEntity;
import dev.micalobia.fullslabs.util.MixedType;
import dev.micalobia.fullslabs.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public interface SlabLike {
    /**
     * Returns a single slab in the specified direcition based on an existing one
     *
     * @param isTowards Whether to get the half towards the facing direcition
     * @return The half specified by isTowards. Can return {@code Blocks.AIR} and {@code Blocks.WATER}. Will not return a waterlogged slab
     */
    BlockState getHalf(BlockState state, BlockGetter level, BlockPos pos, boolean isTowards);

    default BlockState getHalf(BlockState state, BlockGetter level, BlockPos pos, HitResult hitResult) {
        return getHalf(state, level, pos, isHitTowards(state, pos, hitResult));
    }

    default BlockState getHalf(BlockState state, BlockGetter level, BlockPos pos, Vec3 hit) {
        return getHalf(state, level, pos, isHitTowards(state, pos, hit));
    }

    default Pair<BlockState> getHalves(BlockState state, BlockGetter level, BlockPos pos) {
        return new Pair<>(
                getHalf(state, level, pos, false),
                getHalf(state, level, pos, true)
        );
    }


    /**
     * Replaces one half of the block, turning the slab into a mixed slab if necessary
     *
     * @param block The block to replace the half with. Can be a any slab, water or air.
     * @return Whether placement was a success. Returns false if block isn't a slab, water or air.
     */
    default <T extends BlockGetter & LevelWriter> boolean replaceHalf(BlockState state, T level, BlockPos pos, boolean isTowards, Block block) {
        // If we try to replace a half with the same half, exit early
        if (block == state.getBlock()) return false;
        var replacedState = getHalf(state, level, pos, isTowards);
        // Same deal, exit early if we replace with same block
        if (block == replacedState.getBlock()) return false;
        var remainingState = getHalf(state, level, pos, !isTowards);
        var water = block == Blocks.WATER;
        // Removing one half or replacing with water
        if (block == Blocks.AIR || water) {
            var replacingState = remainingState.isAir() && water ?
                    Blocks.WATER.defaultBlockState() :
                    remainingState.trySetValue(BlockStateProperties.WATERLOGGED, water);
            return level.setBlock(pos, replacingState, Block.UPDATE_ALL_IMMEDIATE);
        }
        // Exit early if block isn't a slab at this point
        if (!(block instanceof SlabLike slab) || slab.isMixed()) return false;
        var type = getType(state);
        var replacingState = type.state(slab.getRoot(), isTowards);
        var waterlogged = remainingState.is(Blocks.WATER);
        // If water or air is left over, just place the replacing block
        if (waterlogged || remainingState.isAir())
            return level.setBlock(pos, replacingState.setValue(BlockStateProperties.WATERLOGGED, waterlogged), Block.UPDATE_ALL_IMMEDIATE);
        // If the block we're placing is the same as the block we're keeping, double it up
        if (replacingState.getBlock() == remainingState.getBlock() && isUnmixed()) // Don't merge mixed slabs
            return level.setBlock(pos, slab.asDouble(replacingState), Block.UPDATE_ALL_IMMEDIATE);
        var success = level.setBlock(pos, SlabRegistry.MIXED_SLAB.defaultBlockState().setValue(MixedSlabBlock.TYPE, type), Block.UPDATE_ALL_IMMEDIATE | Block.UPDATE_KNOWN_SHAPE);
        if (!success && !isMixed()) return false;
        var blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof MixedSlabBlockEntity mixedEntity))
            throw new IllegalStateException("Missing MixedSlabBlockEntity!");
        mixedEntity.setBlock(block, isTowards);
        mixedEntity.setBlock(remainingState.getBlock(), !isTowards);
        return true;
    }

    boolean isVanilla();

    boolean isVertical();

    boolean isMixed();

    default boolean isUnmixed() {
        return !isMixed();
    }

    boolean supportsMixing();

    boolean hasVertical();

    boolean isDouble(BlockState state);

    boolean isSingle(BlockState state);

    boolean isTowards(BlockState state);

    MixedType getType(BlockState state);

    Direction getDirection(BlockState state);

    SlabBlock getRoot();

    BlockState asDouble(BlockState state);

    boolean isInside(BlockState state, BlockPos pos, Vec3 hit);

    default boolean isHitTowards(BlockState state, BlockPos pos, HitResult hitResult) {
        return isHitTowards(state, pos, hitResult.getLocation());
    }

    default boolean isHitTowards(BlockState state, BlockPos pos, Vec3 hit) {
        return getType(state).isAxisTargetTowards(hit, pos);
    }

    static boolean isDoubleSlab(BlockState state) {
        return state.getBlock() instanceof SlabLike slab && slab.isDouble(state);
    }

    static boolean isSingleSlab(BlockState state) {
        return state.getBlock() instanceof SlabLike slab && slab.isSingle(state);
    }
}
