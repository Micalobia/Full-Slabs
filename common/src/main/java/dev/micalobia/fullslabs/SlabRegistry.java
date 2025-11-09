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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.WeatheringCopperSlabBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
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

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(FullSlabs.MODID, Registries.BLOCK);
    private static final DeferredRegister<Block> GENERATED = DeferredRegister.create(FullSlabs.MODID, Registries.BLOCK);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(FullSlabs.MODID, Registries.BLOCK_ENTITY_TYPE);

    public static final RegistrySupplier<MixedSlabBlock> MIXED_SLAB = registerBlock("mixed_slab", MixedSlabBlock::new);
    public static final RegistrySupplier<BlockEntityType<MixedSlabBlockEntity>> MIXED_SLAB_ENTITY = BLOCK_ENTITIES.register(
            FullSlabs.id("mixed_slab"),
            () -> BlockEntityTypeAccessor.constructor(MixedSlabBlockEntity::new, Set.of(MIXED_SLAB.get()))
    );

    private static <T extends Block> RegistrySupplier<T> registerBlock(String id, Function<Properties, T> func) {
        return registerBlock(id, func, Properties::of);
    }

    private static <T extends Block> RegistrySupplier<T> registerBlock(String id, Function<Properties, T> func, Supplier<Properties> settings) {
        return BLOCKS.register(id, () -> func.apply(settings.get().setId(generateKey(id))));
    }

    public static void init() {
        registerVanilla();
        initSlabListener();
//        registerDebug();
        seedExistingSlabs();
        BLOCKS.register();
        BLOCK_ENTITIES.register();
        GENERATED.register();
        LifecycleEvent.SETUP.register(() -> VerticalSlabBlock.MAP_VIEW.keySet().stream().filter(slab -> POST_INIT.containsKey(slab.getClass())).forEach(slab -> {
            //noinspection unchecked
            POST_INIT.get(slab.getClass()).consume(slab, VerticalSlabBlock.getVertical(slab));
        }));
    }

    private static void registerVanilla() {
        registerVertical(SlabBlock.class, VerticalSlabBlock::new);
        MixedHandlers.register(SlabBlock.class, VanillaMixedHandler.INSTANCE);
        registerVertical(WeatheringCopperSlabBlock.class, OxidizableVerticalSlabBlock::new, SlabRegistry::registerOxidizableSlabs);
        MixedHandlers.register(WeatheringCopperSlabBlock.class, OxidizableMixedHandler.INSTANCE);
    }

    @SuppressWarnings("unused")
    private static void registerDebug() {
        SlabRegistry.registerBlock("debug", Block::new);
        SlabRegistry.registerBlock("debug_slab", SlabBlock::new);
    }

    private static ResourceKey<Block> generateKey(String path) {
        return generateKey(FullSlabs.id(path));
    }

    private static ResourceKey<Block> generateKey(ResourceLocation id) {
        return ResourceKey.create(Registries.BLOCK, id);
    }

    @ExpectPlatform
    public static void initSlabListener() {
        throw new AssertionError();
    }

    public static void registerOxidizableBlockPair(Block less, Block more) {
        Objects.requireNonNull(less, "Oxidizable block cannot be null!");
        Objects.requireNonNull(more, "Oxidizable block cannot be null!");
        WeatheringCopper.NEXT_BY_BLOCK.get().forcePut(less, more);
    }

    public static void registerWaxableBlockPair(Block unwaxed, Block waxed) {
        Objects.requireNonNull(unwaxed, "Unwaxed block cannot be null!");
        Objects.requireNonNull(waxed, "Waxed block cannot be null!");
        HoneycombItem.WAXABLES.get().forcePut(unwaxed, waxed);
    }

    // This is probably more aggresive than is required, but this is what finally ended up working
    private static void registerOxidizableSlabs(SlabBlock slab, VerticalSlabBlock vertical) {
        var less = WeatheringCopper.getPrevious(slab);
        var more = WeatheringCopper.getNext(slab);
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
        var slabs = BuiltInRegistries.BLOCK.stream().filter(SlabBlock.class::isInstance).toList();
        for (var block : slabs) tryRegisterVertical(BuiltInRegistries.BLOCK.getKey(block), block);
    }

    @ApiStatus.Internal
    public static void tryRegisterVertical(ResourceLocation id, Block block) {
        if (!(block instanceof SlabBlock slab)) return;
        var factory = MAPPING.get(slab.getClass());
        if (factory == null) {
            FullSlabs.LOGGER.warn("{} ({}) failed to register a vertical!", slab, slab.getClass().getSimpleName());
            return;
        }
        var verticalId = FullSlabs.id(FullSlabs.verticalPath(id));

        GENERATED.register(verticalId, () -> {
            var settings = Properties.ofFullCopy(slab).setId(generateKey(verticalId)).overrideLootTable(slab.getLootTable());
            //noinspection unchecked
            return factory.create(slab, settings);
        });
    }

    public interface VerticalFactory<S extends SlabBlock, V extends VerticalSlabBlock> {
        V create(S slab, Properties settings);
    }

    public interface PairConsumer<S extends SlabBlock, V extends VerticalSlabBlock> {
        void consume(S slab, V vertical);
    }
}
