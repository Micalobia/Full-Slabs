package dev.micalobia.fullslabs.fabric;

import dev.micalobia.fullslabs.SlabRegistryBridge;
import dev.micalobia.fullslabs.VerticalSlabBlock;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.registry.Registries;

public class SlabRegistryBridgeImpl {
    private SlabRegistryBridgeImpl() {
    }

    public static void initSlabListener() {
        RegistryEntryAddedCallback.event(Registries.BLOCK).register((i, identifier, block) -> SlabRegistryBridge.tryRegisterVertical(identifier, block));
    }

    public static void postInit() {
        var weathered = VerticalSlabBlock.getVertical((SlabBlock) Blocks.WEATHERED_CUT_COPPER_SLAB);
        var oxidized = VerticalSlabBlock.getVertical((SlabBlock) Blocks.OXIDIZED_CUT_COPPER_SLAB);
        var waxed = VerticalSlabBlock.getVertical((SlabBlock) Blocks.WAXED_OXIDIZED_CUT_COPPER_SLAB);
        OxidizableBlocksRegistry.registerOxidizableBlockPair(weathered, oxidized);
        OxidizableBlocksRegistry.registerWaxableBlockPair(oxidized, waxed);
    }
}
