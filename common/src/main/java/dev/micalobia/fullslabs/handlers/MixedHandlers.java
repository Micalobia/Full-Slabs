package dev.micalobia.fullslabs.handlers;

import dev.micalobia.fullslabs.FullSlabs;
import dev.micalobia.fullslabs.block.SlabLike;
import dev.micalobia.fullslabs.block.VerticalSlabBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Optional;

public final class MixedHandlers {
    private static final HashMap<ResourceLocation, MixedHandlerFactory> ID_HANDLERS = new HashMap<>();
    private static final HashMap<SlabBlock, MixedHandlerFactory> BLOCK_HANDLERS = new HashMap<>();
    private static final HashMap<Class<? extends SlabBlock>, MixedHandlerFactory> CLASS_HANDLERS = new HashMap<>();
    private static final HashMap<SlabBlock, MixedHandler> HANDLERS = new HashMap<>();

    // This governs whether a slab can be mixed at all
    public static boolean hasHandler(Block block) {
        if (!(block instanceof SlabLike slab) || slab.isMixed()) return false;
        var handler = get(block);
        if (handler instanceof VanillaMixedHandler vanilla) return vanilla.valid;
        return handler != null;
    }

    public static @Nullable MixedHandler get(Block block) {
        if (!(block instanceof SlabLike slab) || slab.isMixed() || !slab.hasVertical()) return null;
        var root = slab.getRoot();
        var handler = HANDLERS.get(root);
        if (handler != null) return handler;
        resolve(block);
        var factory = BLOCK_HANDLERS.get(root);
        if (factory == null) factory = CLASS_HANDLERS.get(root.getClass());
        if (factory == null) {
            FullSlabs.LOGGER.warn("{} missing mixed handler; Using default", BuiltInRegistries.BLOCK.getId(block));
            factory = s -> VanillaMixedHandler.INVALID;
        }
        handler = factory.create(root);
        if (handler != null) HANDLERS.put(root, handler);
        return handler;
    }

    public static @Nullable MixedHandler get(BlockState state) {return get(state.getBlock());}

    public static Optional<MixedHandler> tryGet(Block block) {return Optional.ofNullable(get(block));}

    public static Optional<MixedHandler> tryGet(BlockState state) {return tryGet(state.getBlock());}

    public static MixedHandler getOrThrow(Block block) {
        var handler = get(block);
        if (handler == null)
            throw new IllegalArgumentException("Missing handler for %s (%s)!".formatted(block, block.getClass().getSimpleName()));
        return handler;
    }

    public static MixedHandler getOrThrow(BlockState state) {return getOrThrow(state.getBlock());}

    public static void register(ResourceLocation identifier, MixedHandler handler) {
        register(identifier, slab -> handler);
    }

    public static void register(ResourceLocation identifier, MixedHandlerFactory factory) {
        BuiltInRegistries.BLOCK.getOptional(identifier).ifPresentOrElse(
                block -> register(block, factory),
                () -> ID_HANDLERS.put(identifier, factory)
        );
    }

    public static void register(Block block, MixedHandler handler) {
        register(block, slab -> handler);
    }

    public static void register(Block block, MixedHandlerFactory factory) {
        if (block instanceof SlabBlock slab) BLOCK_HANDLERS.put(slab, factory);
        else if (block instanceof VerticalSlabBlock slab) BLOCK_HANDLERS.put(slab.parent, factory);
        else throw new IllegalArgumentException("Tried to register a handler for a block that wasn't a slab!");
    }

    public static <T extends SlabBlock> void register(Class<T> klass, MixedHandler handler) {
        register(klass, slab -> handler);
    }

    public static <T extends SlabBlock> void register(Class<T> klass, MixedHandlerFactory factory) {
        CLASS_HANDLERS.put(klass, factory);
    }

    private static void resolve(ResourceLocation id) {
        BuiltInRegistries.BLOCK.getOptional(id).ifPresent(block -> resolve(block, id));
    }

    private static void resolve(Block block) {
        resolve(block, BuiltInRegistries.BLOCK.getKey(block));
    }

    private static void resolve(Block block, ResourceLocation id) {
        if (ID_HANDLERS.containsKey(id))
            register(block, ID_HANDLERS.remove(id));
    }

    public interface MixedHandlerFactory {
        MixedHandler create(SlabBlock slab);
    }
}
