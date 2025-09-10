package dev.micalobia;

import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Property;
import virtuoel.statement.api.StateRefresher;

public final class FullSlabs {
    public static final String MOD_ID = "fullslabs";

    public static void init() {
    }

    private static <B extends Block, P extends Comparable<P>> void injectBlockstateProperty(Class<B> klass, Property<P> p, P defaultValue) {
        Registries.BLOCK.forEach(block -> {
            if (klass.isAssignableFrom(block.getClass()))
                return;
        });
    }
}
