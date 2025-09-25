package dev.micalobia.fullslabs.fabric;

import dev.micalobia.fullslabs.SlabTraits;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.registry.Registries;

public final class SlabTraitsImpl {
    public static void initListener() {
        RegistryEntryAddedCallback.event(Registries.BLOCK).register((i, identifier, block) -> {
            SlabTraits.resolvePending(identifier, block);
        });
    }
}
