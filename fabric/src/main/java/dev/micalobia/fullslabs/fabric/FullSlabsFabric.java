package dev.micalobia.fullslabs.fabric;

import dev.micalobia.fullslabs.FullSlabs;
import net.fabricmc.api.ModInitializer;

public final class FullSlabsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        FullSlabs.init();
    }
}
