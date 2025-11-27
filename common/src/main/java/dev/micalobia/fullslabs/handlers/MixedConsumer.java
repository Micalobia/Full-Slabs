package dev.micalobia.fullslabs.handlers;

import dev.micalobia.fullslabs.util.SlabContext;

@FunctionalInterface
public interface MixedConsumer {
    void apply(SlabContext context);
}
