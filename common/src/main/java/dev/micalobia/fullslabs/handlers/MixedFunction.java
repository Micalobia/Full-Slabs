package dev.micalobia.fullslabs.handlers;

@FunctionalInterface
public interface MixedFunction<T, C extends MixedContext> {
    T apply(C context);
}
