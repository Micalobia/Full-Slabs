package dev.micalobia;

import dev.architectury.registry.registries.DeferredRegister;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SlabBlock;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.function.BiFunction;

public final class FullSlabs {
    public static final String MODID = "fullslabs";

    public static void init() {
        Registries.BLOCK.forEach(block -> {
            if (block instanceof SlabBlock slab && VerticalSlabBlock.isPure(slab))
                Registry.registerVertical(slab, VerticalSlabBlock::new);
        });
        FullSlabs.Registry.register();
    }

    public static Identifier id(String path) {
        return Identifier.of(FullSlabs.MODID, path);
    }

    public static class Registry {
        public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(FullSlabs.MODID, RegistryKeys.BLOCK);

        public static void register() {
            BLOCKS.register();
        }

        public static <T extends Block> void registerVertical(SlabBlock parent, BiFunction<SlabBlock, AbstractBlock.Settings, T> factory) {
            var id = id(Registries.BLOCK.getId(parent).toString().replace(':', '/'));
            BLOCKS.register(id, () -> {
                RegistryKey<Block> key = RegistryKey.of(RegistryKeys.BLOCK, id);
                var settings = AbstractBlock.Settings.copy(parent).registryKey(key);
                return factory.apply(parent, settings);
            });
        }
    }
}
