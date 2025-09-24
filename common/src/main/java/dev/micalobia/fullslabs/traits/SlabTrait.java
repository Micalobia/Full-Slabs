package dev.micalobia.fullslabs.traits;

import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;


public interface SlabTrait {
    SlabBlock parent();

    default Requirements requirements() {
        return Requirements.EMPTY;
    }

    default void postInit() {
    }

    default void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (requirements().randomTicks) throw new IllegalStateException("Trait requires random ticks!");
    }

    default int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        if (requirements().redstonePower) throw new IllegalStateException("Trait requires strong redstone power!");
        return 0;
    }

    default int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        if (requirements().redstonePower) throw new IllegalStateException("Trait requires weak redstone power!");
        return 0;
    }

    default boolean requiresRandomTicks() {
        return requirements().randomTicks;
    }

    default boolean requiresRedstonePower() {
        return requirements().redstonePower;
    }

    record Requirements(boolean randomTicks, boolean redstonePower) {
        public static Requirements EMPTY = new Requirements(false, false);

        public Requirements add(Requirements other) {
            return new Requirements(
                    this.randomTicks || other.randomTicks,
                    this.redstonePower || other.redstonePower
            );
        }

        public Requirements withRandomTicks() {
            return new Requirements(true, this.redstonePower);
        }

        public Requirements withRedstonePower() {
            return new Requirements(this.randomTicks, true);
        }
    }
}
