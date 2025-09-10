package dev.micalobia.fabric;

import net.fabricmc.api.ModInitializer;

import dev.micalobia.FullSlabs;

public final class FullSlabsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        FullSlabs.init();
    }
}
