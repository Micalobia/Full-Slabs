package dev.micalobia.fullslabs.fabric;

import dev.micalobia.fullslabs.SlabRegistry;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.core.registries.BuiltInRegistries;

public class SlabRegistryImpl {
    private SlabRegistryImpl() {
    }

    public static void initSlabListener() {
        RegistryEntryAddedCallback.event(BuiltInRegistries.BLOCK).register((i, identifier, block) -> SlabRegistry.tryRegisterVertical(identifier, block));
    }
}
