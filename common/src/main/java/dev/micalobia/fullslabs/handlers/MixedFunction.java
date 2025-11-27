package dev.micalobia.fullslabs.handlers;

import dev.micalobia.fullslabs.util.SlabContext;

@FunctionalInterface
public interface MixedFunction<T> {
    T apply(SlabContext context);
}
