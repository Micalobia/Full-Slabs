package dev.micalobia.fullslabs.util;

import org.jetbrains.annotations.Nullable;

public record Result(boolean pure, @Nullable String message) {
    public static Result success() {
        return new Result(true, null);
    }

    public static Result fail(String msg) {
        return new Result(false, msg);
    }
}
