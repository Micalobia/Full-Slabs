package dev.micalobia.fullslabs.util;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

// Yet another pair class, monotype with some Consumer<T>, Function<T, U> and BiFunction<T, T, U> helpers
public record Pair<T>(T left, T right) {
    public void accept(Consumer<T> consumer) {
        consumer.accept(this.left);
        consumer.accept(this.right);
    }

    public <U> Pair<U> map(Function<T, U> function) {
        return new Pair<>(
                function.apply(this.left),
                function.apply(this.right)
        );
    }

    public <U> U merge(BiFunction<T, T, U> function) {
        return function.apply(this.left, this.right);
    }
}
