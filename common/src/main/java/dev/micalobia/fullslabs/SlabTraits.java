package dev.micalobia.fullslabs;

import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.micalobia.fullslabs.traits.OxidizableTrait;
import dev.micalobia.fullslabs.traits.SlabTrait;
import dev.micalobia.fullslabs.traits.SlabTrait.Requirements;
import net.minecraft.block.Block;
import net.minecraft.block.Oxidizable;
import net.minecraft.block.OxidizableSlabBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.item.HoneycombItem;
import net.minecraft.registry.Registries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SlabTraits {
    private final static Map<SlabBlock, List<SlabTrait>> REGISTRY = new HashMap<>();
    private final static Map<SlabBlock, Requirements> REQUIREMENTS = new HashMap<>();

    public static void postInit() {
        for (var slab : REGISTRY.keySet()) {
            for (var trait : REGISTRY.get(slab)) {
                trait.postInit();
            }
        }
    }

    @ExpectPlatform
    public static void initListener() {
        throw new AssertionError();
    }

    public static void seedExisting() {
        Registries.BLOCK.stream().filter(block -> block instanceof OxidizableSlabBlock).map(block -> (OxidizableSlabBlock) block).forEach(SlabTraits::registerOxidizableSlab);
    }

    private static void registerOxidizableSlab(Block block) {
        if (!(block instanceof OxidizableSlabBlock slab)) throw new IllegalArgumentException("Not an oxidizable slab!");
        register(slab, new OxidizableTrait(slab));
    }

    public static void register(SlabBlock parent, SlabTrait trait) {
        REGISTRY.computeIfAbsent(parent, slab -> new ArrayList<>()).add(trait);
        REQUIREMENTS.compute(parent, (block, old) -> old == null ? trait.requirements() : old.add(trait.requirements()));
    }

    public static boolean hasTraits(SlabBlock block) {
        return !REGISTRY.getOrDefault(block, List.of()).isEmpty();
    }

    public static Requirements requirements(SlabBlock block) {
        return REQUIREMENTS.getOrDefault(block, Requirements.EMPTY);
    }

    public static List<SlabTrait> traits(SlabBlock block) {
        return REGISTRY.getOrDefault(block, List.of());
    }

    @FunctionalInterface
    public interface SlabTraitFactory {
        SlabTrait create(SlabBlock slab);
    }
}
