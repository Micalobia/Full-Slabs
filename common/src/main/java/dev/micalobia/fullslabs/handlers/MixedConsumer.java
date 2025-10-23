package dev.micalobia.fullslabs.handlers;

@FunctionalInterface
public interface MixedConsumer<C extends MixedContext> {
    void apply(C context);
}
