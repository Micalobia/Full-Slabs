package dev.micalobia.fullslabs.fabric;

import dev.micalobia.fullslabs.SlabTraits;
import dev.micalobia.fullslabs.traits.OxidizableTrait;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.block.Block;
import net.minecraft.block.OxidizableSlabBlock;
import net.minecraft.registry.Registries;

public final class SlabTraitsImpl {
    public static void initListener() {
        RegistryEntryAddedCallback.event(Registries.BLOCK).register((i, identifier, block) -> tryAddOxidizableTrait(block));
    }

    private static void tryAddOxidizableTrait(Block block) {
        if (!(block instanceof OxidizableSlabBlock slab)) return;
        SlabTraits.register(slab, new OxidizableTrait(slab));
    }
}
