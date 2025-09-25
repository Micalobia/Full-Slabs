package dev.micalobia.fullslabs.fabric;

import dev.micalobia.fullslabs.SlabRegistryBridge;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.registry.Registries;

public class SlabRegistryBridgeImpl {
    private SlabRegistryBridgeImpl() {
    }

    public static void initSlabListener() {
        RegistryEntryAddedCallback.event(Registries.BLOCK).register((i, identifier, block) -> SlabRegistryBridge.tryRegisterVertical(identifier, block));
    }

    public static void postInit() {
    }
}
