package dev.micalobia.fullslabs.util;

import dev.micalobia.fullslabs.SlabRegistry;
import dev.micalobia.fullslabs.block.MixedSlabBlock;
import dev.micalobia.fullslabs.block.VerticalSlabBlock;
import dev.micalobia.fullslabs.block.VerticalSlabBlock.VerticalType;
import dev.micalobia.fullslabs.block.entity.MixedSlabBlockEntity;
import dev.micalobia.fullslabs.handlers.MixedHandler;
import dev.micalobia.fullslabs.handlers.MixedHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public final class SlabContext {
    private static final Supplier<RuntimeException> MISSING_BE = () -> new RuntimeException("Missing mixed slab block entity!");

    private final BlockGetter level;
    private final BlockPos pos;
    private final MixedType type;
    private Side side;
    private BlockState rootState;
    private @Nullable MixedSlabBlockEntity blockEntity;
    private BlockState mainState;
    private BlockState otherState;

    private SlabContext(BlockGetter level, BlockPos pos, Side side) {
        this.rootState = level.getBlockState(pos);
        this.type = MixedType.fromState(this.rootState);
        this.pos = pos;
        this.level = level;
        this.side = side;
    }

    public static SlabContext create(BlockGetter level, BlockPos pos, Side side) {
        return new SlabContext(level, pos, side);
    }

    public Optional<MixedSlabBlockEntity> blockEntity() {return Optional.ofNullable(this.blockEntity);}

    public MixedSlabBlockEntity blockEntityOrThrow() {
        if (this.blockEntity == null) throw MISSING_BE.get();
        return this.blockEntity;
    }

    public Side side() {return this.side;}

    // Is allowed to be either Air or a slab block
    // Will default to stone slab if the block entity is missing

    public BlockState state(Side side) {
        var isTowards = side.isTowards();
        var isMain = side == this.side;
        var state = isMain ? this.mainState : this.otherState;
        if (state != null) return state;
        var rootBlock = this.rootState.getBlock();
        if (rootBlock == SlabRegistry.MIXED_SLAB) {
            if (this.blockEntity == null)
                state = this.rootState.getValue(MixedSlabBlock.TYPE).state((SlabBlock) Blocks.STONE_SLAB, isTowards);
            else state = this.blockEntity.getState(isTowards);
        } else if (Utility.isDoubleSlab(this.rootState))
            state = Utility.setSlabTowards(this.rootState, isTowards);
        else if (Utility.isSlabTowards(this.rootState) == isTowards)
            state = this.rootState.trySetValue(BlockStateProperties.WATERLOGGED, false);
        else state = Blocks.AIR.defaultBlockState();
        if (isMain) this.mainState = state;
        else this.otherState = state;
        return state;
    }

    public SlabContext flip() {
        this.side = this.side.flip();
        return this;
    }

    public BlockState rootState() {return this.rootState;}

    public BlockState mainState() {return this.state(this.side);}

    public BlockState otherState() {return this.state(this.side.flip());}

    public Block block(Side side) {return this.state(side).getBlock();}

    public Block rootBlock() {return this.rootState.getBlock();}

    public Block mainBlock() {return mainState().getBlock();}

    public Block otherBlock() {return otherState().getBlock();}

    public MixedHandler handler(Side side) {
        return MixedHandlers.getOrThrow(block(side));
    }

    public MixedHandler mainHandler() {return handler(this.side);}

    public MixedHandler otherHandler() {return handler(this.side.flip());}

    public boolean replaceMain(Block block) {return replaceSide(block, this.side);}

    public boolean replaceOther(Block block) {return replaceSide(block, this.side.flip());}

    // Does not merge matching slabs in a mixed slab intentionally
    public boolean replaceSide(Block block, Side side) {
        if (!(Utility.isSlabWithVertical(block) || block == Blocks.AIR || block == Blocks.WATER)) return false;
        if (!(this.level instanceof LevelWriter writer)) return false;
        var isTowards = side.isTowards();
        var rootBlock = this.rootBlock();
        var waterlogged = this.rootState.getValueOrElse(BlockStateProperties.WATERLOGGED, this.rootState.is(Blocks.WATER));
        var replacedState = state(side);
        var keepState = state(side.flip()).trySetValue(BlockStateProperties.WATERLOGGED, waterlogged);
        if (this.rootState.isAir() || this.rootState.is(Blocks.WATER)) {
            if (!Utility.isSlabWithVertical(block)) return false;
            var slab = VerticalSlabBlock.getRoot(block);
            var state = this.type.state(slab, isTowards).setValue(BlockStateProperties.WATERLOGGED, waterlogged);
            var success = writer.setBlock(this.pos, state, Block.UPDATE_ALL);
            if (!success) return false;
            this.rootState = state;
            this.mainState = this.otherState = null;
            return true;
        }
        if (block == Blocks.AIR || block == Blocks.WATER) {
            if (replacedState.isAir()) return false;
            var state = keepState.isAir() && waterlogged ? Blocks.WATER.defaultBlockState() : keepState;
            var success = writer.setBlock(this.pos, state, Block.UPDATE_ALL);
            if (!success) return false;
            this.rootState = state;
            this.blockEntity = null;
            this.mainState = this.otherState = null;
            return true;
        }
        if (rootBlock == SlabRegistry.MIXED_SLAB) {
            if (this.blockEntity == null) {
                var blockEntity = this.level.getBlockEntity(this.pos);
                if (!(blockEntity instanceof MixedSlabBlockEntity mixedEntity)) return false;
                this.blockEntity = mixedEntity;
            }
            var success = this.blockEntity.setBlock(block, isTowards);
            if (!success) return false;
            this.mainState = this.otherState = null;
            return true;
        }
        if (replacedState.isAir() && keepState.is(block)) {
            var rootState = doubleSlab(keepState);
            var success = writer.setBlock(this.pos, rootState, Block.UPDATE_ALL);
            if (!success) return false;
            this.rootState = rootState;
            this.mainState = this.otherState = null;
            return true;
        }
        if (Utility.isDoubleSlab(this.rootState)) {
            if (block == rootBlock) return false;
            var type = MixedType.fromState(this.rootState);
            var rootState = SlabRegistry.MIXED_SLAB.defaultBlockState().setValue(MixedSlabBlock.TYPE, type);
            var success = writer.setBlock(this.pos, rootState, Block.UPDATE_ALL);
            if (!success) return false;
            var blockEntity = Objects.requireNonNull((MixedSlabBlockEntity) this.level.getBlockEntity(this.pos));
            success = blockEntity.setBlocks(
                    isTowards ? block : rootBlock,
                    isTowards ? rootBlock : block
            );
            if (!success) return false;
            this.blockEntity = blockEntity;
            this.rootState = rootState;
            this.mainState = this.otherState = null;
            return true;
        }
        return false;
    }

    public enum Side {
        TOWARDS, AWAY;

        public boolean isTowards() {return this == TOWARDS;}

        public Side flip() {return this == TOWARDS ? AWAY : TOWARDS;}

        public static Side fromTowards(boolean isTowards) {return isTowards ? TOWARDS : AWAY;}
    }

    public static BlockState doubleSlab(BlockState state) {
        return switch (state.getBlock()) {
            case SlabBlock ignored -> state.setValue(BlockStateProperties.SLAB_TYPE, SlabType.DOUBLE);
            case VerticalSlabBlock ignored -> state.setValue(VerticalSlabBlock.TYPE, VerticalType.FULL);
            default -> state;
        };
    }
}
