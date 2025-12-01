package dev.micalobia.fullslabs.ducks;

import dev.micalobia.fullslabs.handlers.MixedConsumer;
import dev.micalobia.fullslabs.handlers.MixedFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;

import java.util.function.BiFunction;

public interface MixedSlabBlockDuck {
    <T> T forward(BlockGetter world, BlockPos pos, MixedFunction<T> function);

    <T> T forwardSideValue(BlockGetter world, BlockPos pos, boolean towards, MixedFunction<T> function);

    <T> T forwardSideValue(BlockGetter world, BlockPos pos, Vec3 hit, MixedFunction<T> function);

    void forwardSide(BlockGetter world, BlockPos pos, boolean towards, MixedConsumer consumer);

    void forwardSide(BlockGetter world, BlockPos pos, Vec3 hit, MixedConsumer consumer);

    <T, R> R forwardSidesValue(BlockGetter world, BlockPos pos, MixedFunction<T> function, BiFunction<T, T, R> selector);

    void forwardSides(BlockGetter world, BlockPos pos, MixedConsumer consumer);
}
