package dev.micalobia.fullslabs.fabric;

import dev.micalobia.fullslabs.SlabTraits;
import dev.micalobia.fullslabs.VerticalSlabBlock;
import dev.micalobia.fullslabs.traits.OxidizableTrait;
import dev.micalobia.fullslabs.traits.SlabTrait;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.Oxidizable;
import net.minecraft.block.OxidizableSlabBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.item.HoneycombItem;
import net.minecraft.registry.Registries;

public final class SlabTraitsImpl {
    public static void initListener() {
        RegistryEntryAddedCallback.event(Registries.BLOCK).register((i, identifier, block) -> tryAddOxidizableTrait(block));
    }

    public static void postInit(SlabBlock slab, SlabTrait trait) {
        if (trait instanceof OxidizableTrait) {
            var original = VerticalSlabBlock.getVertical(slab);
            Oxidizable.getIncreasedOxidationBlock(slab).ifPresent(block -> OxidizableBlocksRegistry.registerOxidizableBlockPair(original, VerticalSlabBlock.getVertical((SlabBlock) block)));
            HoneycombItem.getWaxedState(slab.getDefaultState()).ifPresent(state -> OxidizableBlocksRegistry.registerWaxableBlockPair(original, VerticalSlabBlock.getVertical((SlabBlock) state.getBlock())));
        }
    }

    private static void tryAddOxidizableTrait(Block block) {
        if (!(block instanceof OxidizableSlabBlock slab)) return;
        SlabTraits.register(slab, new OxidizableTrait(slab));
    }
}
