package dev.micalobia.fullslabs;

import dev.architectury.registry.registries.DeferredRegister;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SlabBlock;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.BiFunction;

public final class FullSlabs {
    public static final String MODID = "fullslabs";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public static void init() {
        Registries.BLOCK.forEach(FullSlabs::register);
    }

    public static void finish() {
        Registry.BLOCKS.register();;
    }

    public static void register(Block block) {
        if (!(block instanceof SlabBlock slab)) return;
        var pure = VerticalSlabBlock.isPure(slab);
        if (pure.pure()) Registry.registerVertical(slab, VerticalSlabBlock::new);
        else LOGGER.warn("{} isn't pure because of: '{}'", Registries.BLOCK.getId(block), pure.message());
    }

    public static Identifier id(String path) {
        return Identifier.of(FullSlabs.MODID, path);
    }

    public static String verticalPath(Identifier parent) {
        return "vertical/" + parent.toString().replace(':', '/');
    }

    public static class Registry {
        public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(FullSlabs.MODID, RegistryKeys.BLOCK);

        public static void register() {
            BLOCKS.register();
        }

        public static <T extends Block> void registerVertical(SlabBlock parent, BiFunction<SlabBlock, AbstractBlock.Settings, T> factory) {
            var id = id(verticalPath(Registries.BLOCK.getId(parent)));
            BLOCKS.register(id, () -> {
                RegistryKey<Block> key = RegistryKey.of(RegistryKeys.BLOCK, id);
                var settings = AbstractBlock.Settings.copy(parent).registryKey(key);
                return factory.apply(parent, settings);
            });
        }
    }
}
