package dev.micalobia.fullslabs;

import dev.architectury.event.events.common.LootEvent;
import dev.micalobia.fullslabs.compat.FullSlabsCompat;
import dev.micalobia.fullslabs.config.Config;
import dev.micalobia.fullslabs.config.Controls;
import dev.micalobia.fullslabs.loot.VerticalLootTable;
import eu.midnightdust.lib.config.MidnightConfig;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class FullSlabs {
    public static final String MODID = "fullslabs";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static SlabBlock DEFAULT;

    public static void init() {
        DEFAULT = (SlabBlock) Blocks.STONE_SLAB;
        MidnightConfig.init(MODID, Config.class);
        FullSlabsCompat.init();
        SlabRegistry.init();
        LootEvent.MODIFY_LOOT_TABLE.register(new VerticalLootTable());
        Controls.serverInit();
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(FullSlabs.MODID, path);
    }

    public static String verticalPath(Identifier parent) {
        return "vertical/" + parent.toString().replace(':', '/');
    }
}
