package dev.micalobia.fullslabs.handlers;

import dev.micalobia.fullslabs.SlabRegistry;
import dev.micalobia.fullslabs.block.MixedSlabBlock;
import dev.micalobia.fullslabs.block.entity.MixedSlabBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public sealed interface MixedContext permits MixedContext.Sideless, MixedContext.Sided {
    Supplier<RuntimeException> MISSING_BE = () -> new RuntimeException("Missing mixed slab block entity!");

    BlockState mixedState();

    Optional<MixedSlabBlockEntity> blockEntity();

    boolean replaceBlock(Block block);

    Sided sided(boolean towards);

    default MixedContext requireBlockEntity() {
        if (blockEntity().isEmpty()) throw MISSING_BE.get();
        return this;
    }

    default MixedSlabBlockEntity blockEntityOrThrow() {
        return blockEntity().orElseThrow(MISSING_BE);
    }

    record Sided(
            BlockState mixedState,
            Optional<MixedSlabBlockEntity> blockEntity,
            boolean towards,
            BlockState state
    ) implements MixedContext {
        @Override
        public boolean replaceBlock(Block block) {
            return blockEntity.map(entity -> entity.setBlock(block, towards)).orElse(false);
        }

        public Sided sided(boolean towards) {
            if (this.towards == towards) return this;
            return new Sideless(this.mixedState, this.blockEntity).sided(towards);
        }

        public Block block() {
            return this.state.getBlock();
        }

        public MixedHandler handler() {
            return MixedHandlers.getOrThrow(state.getBlock());
        }
    }

    record Sideless(
            BlockState mixedState,
            Optional<MixedSlabBlockEntity> blockEntity
    ) implements MixedContext {
        @Override
        public boolean replaceBlock(Block block) {
            return false;
        }

        public Sided sided(boolean towards) {
            var type = mixedState.getValue(MixedSlabBlock.TYPE);
            var slab = blockEntity.map(entity -> entity.getBlock(towards)).orElse((SlabBlock) Blocks.STONE_SLAB);
            return new Sided(mixedState, blockEntity, towards, type.state(slab, towards));
        }
    }

    static Sided create(BlockGetter world, BlockPos pos, boolean towards) {
        return create(world, pos).sided(towards);
    }

    static Sideless create(BlockGetter world, BlockPos pos) {
        var block = SlabRegistry.MIXED_SLAB.get();
        var state = world.getBlockState(pos);
        if (!state.is(block)) throw new IllegalArgumentException("This block isn't a mixed slab!");
        var entity = world.getBlockEntity(pos);
        Optional<MixedSlabBlockEntity> optionalEntity;
        if (!(entity instanceof MixedSlabBlockEntity mixed)) {
            optionalEntity = Optional.empty();
        } else {
            optionalEntity = Optional.of(mixed);
        }
        return new Sideless(state, optionalEntity);
    }
}
