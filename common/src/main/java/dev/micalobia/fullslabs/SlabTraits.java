package dev.micalobia.fullslabs;

import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.micalobia.fullslabs.block.VerticalSlabBlock;
import dev.micalobia.fullslabs.traits.OxidizableTrait;
import dev.micalobia.fullslabs.traits.SimpleRedstoneTrait;
import dev.micalobia.fullslabs.traits.SlabTrait;
import dev.micalobia.fullslabs.traits.SlabTrait.Requirements;
import net.minecraft.block.Block;
import net.minecraft.block.OxidizableSlabBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SlabTraits {
    private final static Map<Class<? extends SlabBlock>, List<SlabTraitFactory>> GROUPS = new HashMap<>();
    private final static Map<Identifier, List<SlabTraitFactory>> PENDING = new HashMap<>();
    private final static Map<SlabBlock, List<SlabTrait>> REGISTRY = new HashMap<>();
    private final static Map<SlabBlock, Requirements> REQUIREMENTS = new HashMap<>();

    public static void init() {
        VerticalSlabBlock.MAP_VIEW.values().forEach(VerticalSlabBlock::initializeTraits);
    }

    @ExpectPlatform
    public static void initListener() {
        throw new AssertionError();
    }

    public static void seedExisting() {
        register(OxidizableSlabBlock.class, slab -> new OxidizableTrait((OxidizableSlabBlock) slab));
        register(Identifier.of("blockus:redstone_brick_slab"), slab -> new SimpleRedstoneTrait(slab, 15, 0));
        Registries.BLOCK.getEntrySet().forEach(entry -> resolvePending(entry.getKey().getValue(), entry.getValue()));
    }

    public static void register(SlabBlock parent, SlabTraitFactory factory) {
        var trait = factory.create(parent);
        REGISTRY.computeIfAbsent(parent, slab -> new ArrayList<>()).add(trait);
        REQUIREMENTS.compute(parent, (block, old) -> old == null ? trait.requirements() : old.add(trait.requirements()));
    }

    public static void register(Identifier parent, SlabTraitFactory factory) {
        PENDING.computeIfAbsent(parent, id -> new ArrayList<>()).add(factory);
    }

    public static <T extends SlabBlock> void register(Class<T> klass, SlabTraitFactory factory) {
        GROUPS.computeIfAbsent(klass, cls -> new ArrayList<>()).add(factory);
    }

    public static void resolvePending(Identifier id, Block block) {
        if (!(block instanceof SlabBlock slab)) return;
        if (PENDING.containsKey(id))
            PENDING.remove(id).forEach(factory -> register(slab, factory));

        GROUPS.entrySet().stream().filter(entry -> entry.getKey().isInstance(slab)).forEach(entry -> entry.getValue().forEach(factory -> register(slab, factory)));
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
