package dev.micalobia.fullslabs.traits;

import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;


public interface SlabTrait {
    SlabBlock parent();

    default Requirements requirements() {
        return new Requirements(false);
    }

    default void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (requirements().randomTicks) throw new IllegalStateException("Trait requires random ticks!");
    }

    default boolean requiresRandomTicks() {
        return requirements().randomTicks;
    }

    record Requirements(boolean randomTicks) {
        public static Requirements NONE = new Requirements(false);

        public Requirements add(Requirements other) {
            return new Requirements(randomTicks || other.randomTicks);
        }
    }
}
