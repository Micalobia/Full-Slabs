package dev.micalobia.fullslabs;

import dev.architectury.event.events.common.LootEvent;
import dev.micalobia.fullslabs.config.Config;
import dev.micalobia.fullslabs.loot.VerticalLootTable;
import eu.midnightdust.lib.config.MidnightConfig;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class FullSlabs {
    public static final String MODID = "fullslabs";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public static void init() {
        MidnightConfig.init(MODID, Config.class);
        SlabRegistryBridge.init();
        LootEvent.MODIFY_LOOT_TABLE.register(new VerticalLootTable());
    }

    public static Identifier id(String path) {
        return Identifier.of(FullSlabs.MODID, path);
    }

    public static String verticalPath(Identifier parent) {
        return "vertical/" + parent.toString().replace(':', '/');
    }
}
