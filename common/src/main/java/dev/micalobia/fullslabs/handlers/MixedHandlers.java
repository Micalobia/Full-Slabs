package dev.micalobia.fullslabs.handlers;

import dev.micalobia.fullslabs.block.VerticalSlabBlock;
import dev.micalobia.fullslabs.util.Utility;
import net.minecraft.block.Block;
import net.minecraft.block.SlabBlock;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public final class MixedHandlers {
    private static final HashMap<Identifier, MixedHandlerFactory> ID_HANDLERS = new HashMap<>();
    private static final HashMap<SlabBlock, MixedHandlerFactory> BLOCK_HANDLERS = new HashMap<>();
    private static final HashMap<Class<? extends SlabBlock>, MixedHandlerFactory> CLASS_HANDLERS = new HashMap<>();
    private static final HashMap<SlabBlock, MixedHandler> HANDLERS = new HashMap<>();

    // This governs whether a slab can be mixed at all
    public static boolean hasHandler(Block block) {
        if (!Utility.isSlab(block)) return false;
        var slab = VerticalSlabBlock.tryGetRoot(block);
        return slab.filter(slabBlock -> HANDLERS.containsKey(slabBlock) ||
                BLOCK_HANDLERS.containsKey(slabBlock) ||
                CLASS_HANDLERS.containsKey(slabBlock.getClass()) ||
                ID_HANDLERS.containsKey(Registries.BLOCK.getId(slabBlock))
        ).isPresent();
    }

    public static @Nullable MixedHandler get(Block block) {
        if (!Utility.isSlab(block)) return null;
        var slab = VerticalSlabBlock.getRoot(block);
        var handler = HANDLERS.get(slab);
        if (handler == null) {
            resolve(block);
            handler = BLOCK_HANDLERS.getOrDefault(slab, CLASS_HANDLERS.get(slab.getClass())).create(slab);
            if (handler != null) HANDLERS.put(slab, handler);
        }
        return handler;
    }

    public static MixedHandler getOrThrow(Block block) {
        var handler = get(block);
        if (handler == null)
            throw new IllegalArgumentException("Missing handler for %s (%s)!".formatted(block, block.getClass().getSimpleName()));
        return handler;
    }

    public static void register(Identifier identifier, MixedHandler handler) {
        register(identifier, slab -> handler);
    }

    public static void register(Identifier identifier, MixedHandlerFactory factory) {
        Registries.BLOCK.getOptionalValue(identifier).ifPresentOrElse(
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

    private static void resolve(Identifier id) {
        Registries.BLOCK.getOptionalValue(id).ifPresent(block -> resolve(block, id));
    }

    private static void resolve(Block block) {
        resolve(block, Registries.BLOCK.getId(block));
    }

    private static void resolve(Block block, Identifier id) {
        if (ID_HANDLERS.containsKey(id))
            register(block, ID_HANDLERS.remove(id));
    }

    public interface MixedHandlerFactory {
        MixedHandler create(SlabBlock slab);
    }
}
