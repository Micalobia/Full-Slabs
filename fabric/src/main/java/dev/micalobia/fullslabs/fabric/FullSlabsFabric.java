package dev.micalobia.fullslabs.fabric;

import net.fabricmc.api.ModInitializer;

import dev.micalobia.fullslabs.FullSlabs;

public final class FullSlabsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        FullSlabs.init();
    }
}
