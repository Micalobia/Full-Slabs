package dev.micalobia.fullslabs;

import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.micalobia.fullslabs.block.MixedSlabBlock;
import dev.micalobia.fullslabs.block.OxidizableVerticalSlabBlock;
import dev.micalobia.fullslabs.block.VerticalSlabBlock;
import dev.micalobia.fullslabs.block.entity.MixedSlabBlockEntity;
import dev.micalobia.fullslabs.handlers.MixedHandlers;
import dev.micalobia.fullslabs.handlers.OxidizableMixedHandler;
import dev.micalobia.fullslabs.handlers.VanillaMixedHandler;
import dev.micalobia.fullslabs.mixin.BlockEntityTypeAccessor;
import dev.micalobia.fullslabs.util.Utility;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.Block;
import net.minecraft.block.Oxidizable;
import net.minecraft.block.OxidizableSlabBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.HoneycombItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public class SlabRegistry {
    private SlabRegistry() {
    }

    @SuppressWarnings("rawtypes")
    private static final Map<Class<? extends SlabBlock>, VerticalFactory> MAPPING = new HashMap<>();
    @SuppressWarnings("rawtypes")
    private static final Map<Class<? extends SlabBlock>, PairConsumer> POST_INIT = new HashMap<>();

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(FullSlabs.MODID, RegistryKeys.BLOCK);
    private static final DeferredRegister<Block> GENERATED = DeferredRegister.create(FullSlabs.MODID, RegistryKeys.BLOCK);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(FullSlabs.MODID, RegistryKeys.BLOCK_ENTITY_TYPE);

    public static final RegistrySupplier<MixedSlabBlock> MIXED_SLAB = registerBlock("mixed_slab", MixedSlabBlock::new);
    public static final RegistrySupplier<BlockEntityType<MixedSlabBlockEntity>> MIXED_SLAB_ENTITY = BLOCK_ENTITIES.register(
            FullSlabs.id("mixed_slab"),
            () -> BlockEntityTypeAccessor.constructor(MixedSlabBlockEntity::new, Set.of(MIXED_SLAB.get()))
    );

    private static <T extends Block> RegistrySupplier<T> registerBlock(String id, Function<Settings, T> func) {
        return registerBlock(id, func, Settings::create);
    }

    private static <T extends Block> RegistrySupplier<T> registerBlock(String id, Function<Settings, T> func, Supplier<Settings> settings) {
        return BLOCKS.register(id, () -> func.apply(settings.get().registryKey(generateKey(id))));
    }

    public static void init() {
        registerVanilla();
        initSlabListener();
//        registerDebug();
        seedExistingSlabs();
        BLOCKS.register();
        BLOCK_ENTITIES.register();
        GENERATED.register();
        LifecycleEvent.SETUP.register(() -> {
            VerticalSlabBlock.MAP_VIEW.keySet().stream().filter(slab -> POST_INIT.containsKey(slab.getClass())).forEach(slab -> {
                //noinspection unchecked
                POST_INIT.get(slab.getClass()).consume(slab, VerticalSlabBlock.getVertical(slab));
            });
        });
    }

    private static void registerVanilla() {
        registerVertical(SlabBlock.class, VerticalSlabBlock::new);
        MixedHandlers.register(SlabBlock.class, VanillaMixedHandler.INSTANCE);
        registerVertical(OxidizableSlabBlock.class, OxidizableVerticalSlabBlock::new, SlabRegistry::registerOxidizableSlabs);
        MixedHandlers.register(OxidizableSlabBlock.class, OxidizableMixedHandler.INSTANCE);
    }

    private static void registerDebug() {
        SlabRegistry.registerBlock("debug", Block::new);
        SlabRegistry.registerBlock("debug_slab", SlabBlock::new);
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
        Oxidizable.OXIDATION_LEVEL_INCREASES.get().forcePut(less, more);
    }

    public static void registerWaxableBlockPair(Block unwaxed, Block waxed) {
        Objects.requireNonNull(unwaxed, "Unwaxed block cannot be null!");
        Objects.requireNonNull(waxed, "Waxed block cannot be null!");
        HoneycombItem.UNWAXED_TO_WAXED_BLOCKS.get().forcePut(unwaxed, waxed);
    }

    // This is probably more aggresive than is required, but this is what finally ended up working
    private static void registerOxidizableSlabs(SlabBlock slab, VerticalSlabBlock vertical) {
        var less = Oxidizable.getDecreasedOxidationBlock(slab);
        var more = Oxidizable.getIncreasedOxidationBlock(slab);
        var lessWaxed = less.flatMap(Utility::getWaxed);
        var slabWaxed = Utility.getWaxed(slab);
        var moreWaxed = more.flatMap(Utility::getWaxed);
        var lessVertical = less.flatMap(VerticalSlabBlock::tryGetVertical);
        var moreVertical = more.flatMap(VerticalSlabBlock::tryGetVertical);
        var lessWaxedVertical = lessWaxed.flatMap(VerticalSlabBlock::tryGetVertical);
        var slabWaxedVertical = slabWaxed.flatMap(VerticalSlabBlock::tryGetVertical);
        var moreWaxedVertical = moreWaxed.flatMap(VerticalSlabBlock::tryGetVertical);
        lessVertical.ifPresent(_less -> {
            registerOxidizableBlockPair(_less, vertical);
            lessWaxedVertical.ifPresent(_waxed -> {
                registerWaxableBlockPair(_less, _waxed);
                MixedHandlers.register(_waxed, OxidizableMixedHandler.INSTANCE);
            });
        });
        moreVertical.ifPresent(_more -> {
            registerOxidizableBlockPair(vertical, _more);
            moreWaxedVertical.ifPresent(_waxed -> {
                registerWaxableBlockPair(_more, _waxed);
                MixedHandlers.register(_waxed, OxidizableMixedHandler.INSTANCE);
            });
        });
        slabWaxedVertical.ifPresent(_waxed -> {
            registerWaxableBlockPair(vertical, _waxed);
            MixedHandlers.register(_waxed, OxidizableMixedHandler.INSTANCE);
        });
    }

    public static <S extends SlabBlock, V extends VerticalSlabBlock> void registerVertical(Class<S> slabClass, VerticalFactory<S, V> factory) {
        registerVertical(slabClass, factory, null);
    }

    public static <S extends SlabBlock, V extends VerticalSlabBlock> void registerVertical(Class<S> slabClass, VerticalFactory<S, V> factory, @Nullable PairConsumer<S, V> listener) {
        var old = MAPPING.putIfAbsent(slabClass, factory);
        if (old != null) throw new IllegalArgumentException("That slab class has already been registered!");
        if (listener != null)
            POST_INIT.put(slabClass, listener);
    }

    private static void seedExistingSlabs() {
        var slabs = Registries.BLOCK.stream().filter(SlabBlock.class::isInstance).toList();
        for (var block : slabs) tryRegisterVertical(Registries.BLOCK.getId(block), block);
    }

    @ApiStatus.Internal
    public static void tryRegisterVertical(Identifier id, Block block) {
        if (!(block instanceof SlabBlock slab)) return;
        var factory = MAPPING.get(slab.getClass());
        if (factory == null) {
            FullSlabs.LOGGER.warn("{} ({}) failed to register a vertical!", slab, slab.getClass().getSimpleName());
            return;
        }
        var verticalId = FullSlabs.id(FullSlabs.verticalPath(id));

        var b = GENERATED.register(verticalId, () -> {
            var settings = Settings.copy(slab).registryKey(generateKey(verticalId)).lootTable(slab.getLootTableKey());
            //noinspection unchecked
            return factory.create(slab, settings);
        });
    }

    public interface VerticalFactory<S extends SlabBlock, V extends VerticalSlabBlock> {
        V create(S slab, Settings settings);
    }

    public interface PairConsumer<S extends SlabBlock, V extends VerticalSlabBlock> {
        void consume(S slab, V vertical);
    }
}
