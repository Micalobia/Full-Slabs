package dev.micalobia.fullslabs.fabric;

import dev.micalobia.fullslabs.SlabRegistry;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.registry.Registries;

public class SlabRegistryImpl {
    private SlabRegistryImpl() {
    }

    public static void initSlabListener() {
        RegistryEntryAddedCallback.event(Registries.BLOCK).register((i, identifier, block) -> SlabRegistry.tryRegisterVertical(identifier, block));
    }
}
