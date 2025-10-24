package dev.micalobia.fullslabs.compat;

import dev.architectury.injectables.annotations.ExpectPlatform;

public final class FullSlabsCompat {
    public static void init() {
        platformInit();
    }

    @ExpectPlatform
    public static void platformInit() {
        throw new AssertionError();
    }
}
