package dev.micalobia.fullslabs.ducks;

import dev.micalobia.fullslabs.handlers.MixedConsumer;
import dev.micalobia.fullslabs.handlers.MixedContext;
import dev.micalobia.fullslabs.handlers.MixedFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;

import java.util.function.BiFunction;

@SuppressWarnings("unused")
public interface MixedSlabBlockDuck {
    <T> T forward(BlockGetter world, BlockPos pos, MixedFunction<T, MixedContext.Sideless> function);

    @SuppressWarnings("UnusedReturnValue")
    <T> T forwardSideValue(BlockGetter world, BlockPos pos, boolean towards, MixedFunction<T, MixedContext.Sided> function);

    <T> T forwardSideValue(BlockGetter world, BlockPos pos, Vec3 hit, MixedFunction<T, MixedContext.Sided> function);

    void forwardSide(BlockGetter world, BlockPos pos, boolean towards, MixedConsumer<MixedContext.Sided> consumer);

    void forwardSide(BlockGetter world, BlockPos pos, Vec3 hit, MixedConsumer<MixedContext.Sided> consumer);

    <T, R> R forwardSidesValue(BlockGetter world, BlockPos pos, MixedFunction<T, MixedContext.Sided> function, BiFunction<T, T, R> selector);

    void forwardSides(BlockGetter world, BlockPos pos, MixedConsumer<MixedContext.Sided> consumer);
}
