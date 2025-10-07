package dev.micalobia.fullslabs.handlers;

import dev.micalobia.fullslabs.block.VerticalSlabBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SlabBlock;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public final class MixedHandlers {
    private static final HashMap<Class<? extends SlabBlock>, MixedHandler> HANDLERS = new HashMap<>();

    public static boolean hasHandler(Block block) {
        if (block instanceof SlabBlock slab) return HANDLERS.containsKey(slab.getClass());
        if (block instanceof VerticalSlabBlock slab) return HANDLERS.containsKey(slab.parent.getClass());
        return false;
    }

    public static @Nullable MixedHandler get(Block block) {
        if (block instanceof SlabBlock slab) return HANDLERS.get(slab.getClass());
        if (block instanceof VerticalSlabBlock slab) return HANDLERS.get(slab.parent.getClass());
        return null;
    }

    public static MixedHandler getOrThrow(Block block) {
        var handler = get(block);
        if (handler == null) throw new IllegalArgumentException("Missing handler!");
        return handler;
    }

    public static <T extends SlabBlock> void register(Class<T> klass, MixedHandler handler) {
        HANDLERS.put(klass, handler);
    }
}
