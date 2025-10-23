package dev.micalobia.fullslabs.ducks;

import dev.micalobia.fullslabs.handlers.MixedConsumer;
import dev.micalobia.fullslabs.handlers.MixedContext;
import dev.micalobia.fullslabs.handlers.MixedFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;

import java.util.function.BiFunction;

public interface MixedSlabBlockDuck {
    <T> T forward(BlockView world, BlockPos pos, MixedFunction<T, MixedContext.Sideless> function);

    <T> T forwardSideValue(BlockView world, BlockPos pos, boolean towards, MixedFunction<T, MixedContext.Sided> function);

    <T> T forwardSideValue(BlockView world, BlockPos pos, Vec3d hit, MixedFunction<T, MixedContext.Sided> function);

    void forwardSide(BlockView world, BlockPos pos, boolean towards, MixedConsumer<MixedContext.Sided> consumer);

    void forwardSide(BlockView world, BlockPos pos, Vec3d hit, MixedConsumer<MixedContext.Sided> consumer);

    <T, R> R forwardSidesValue(BlockView world, BlockPos pos, MixedFunction<T, MixedContext.Sided> function, BiFunction<T, T, R> selector);

    void forwardSides(BlockView world, BlockPos pos, MixedConsumer<MixedContext.Sided> consumer);
}
