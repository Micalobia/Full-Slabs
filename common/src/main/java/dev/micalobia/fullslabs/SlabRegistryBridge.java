package dev.micalobia.fullslabs;

import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.architectury.registry.registries.DeferredRegister;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.Block;
import net.minecraft.block.Oxidizable;
import net.minecraft.block.SlabBlock;
import net.minecraft.item.HoneycombItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

public class SlabRegistryBridge {
    private SlabRegistryBridge() {
    }

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(FullSlabs.MODID, RegistryKeys.BLOCK);
    private static final DeferredRegister<Block> GENERATED = DeferredRegister.create(FullSlabs.MODID, RegistryKeys.BLOCK);

    private static final Set<Identifier> QUEUED_VERTICALS = new HashSet<>();

    private static <T extends Block> void registerBlock(String id, Supplier<T> supplier) {
        BLOCKS.register(id, supplier);
    }

    public static void init() {
        SlabTraits.initListener();
        initSlabListener();
//        registerDebug();
        SlabTraits.seedExisting();
        seedExistingSlabs();
        BLOCKS.register();
        GENERATED.register();
        SlabTraits.postInit();
        postInit();
    }

    @ExpectPlatform
    public static void postInit() {
        throw new AssertionError();
    }

    private static void registerDebug() {
        SlabRegistryBridge.registerBlock("debug", () -> new Block(Settings.create().registryKey(generateKey("debug"))));
        SlabRegistryBridge.registerBlock("debug_slab", () -> new SlabBlock(Settings.create().registryKey(generateKey("debug_slab"))));
    }

    private static RegistryKey<Block> generateKey(String path) {
        return generateKey(FullSlabs.id(path));
    }

    private static RegistryKey<Block> generateKey(Identifier id) {
        return RegistryKey.of(RegistryKeys.BLOCK, id);
    }

    @ExpectPlatform
    public static void initSlabListener() {
        throw new AssertionError();
    }

    public static void registerOxidizableBlockPair(Block less, Block more) {
        Objects.requireNonNull(less, "Oxidizable block cannot be null!");
        Objects.requireNonNull(more, "Oxidizable block cannot be null!");
        Oxidizable.OXIDATION_LEVEL_INCREASES.get().put(less, more);
    }

    public static void registerWaxableBlockPair(Block unwaxed, Block waxed) {
        Objects.requireNonNull(unwaxed, "Unwaxed block cannot be null!");
        Objects.requireNonNull(waxed, "Waxed block cannot be null!");
        HoneycombItem.UNWAXED_TO_WAXED_BLOCKS.get().put(unwaxed, waxed);
    }

    public static void tryRegisterVertical(Identifier id, Block block) {
        if (!(block instanceof SlabBlock slab)) return;
        var result = VerticalSlabBlock.isValid(slab);
        var valid = result.value();
        if (!valid) {
            FullSlabs.LOGGER.warn("{} isn't valid: '{}'", id, result.message());
            return;
        }

        var verticalId = FullSlabs.id(FullSlabs.verticalPath(id));
        if (!QUEUED_VERTICALS.add(verticalId)) return;

        GENERATED.register(verticalId, () -> {
            var settings = Settings.copy(slab).registryKey(generateKey(verticalId));
            return new VerticalSlabBlock(slab, settings);
        });
    }

    private static void seedExistingSlabs() {
        var slabs = Registries.BLOCK.stream().filter(block -> block instanceof SlabBlock).toList();
        for (var block : slabs) tryRegisterVertical(Registries.BLOCK.getId(block), block);
    }
}
