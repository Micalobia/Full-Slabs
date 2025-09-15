package dev.micalobia.fullslabs.fabric;

import dev.micalobia.fullslabs.FullSlabs;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.registry.Registries;

public final class FullSlabsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        FullSlabs.init();
        RegistryEntryAddedCallback.event(Registries.BLOCK).register(((i, identifier, block) -> FullSlabs.register(block)));
        FullSlabs.finish();
    }
}
