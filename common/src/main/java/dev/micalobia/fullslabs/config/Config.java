package dev.micalobia.fullslabs.config;


import com.google.common.collect.Lists;
import eu.midnightdust.lib.config.MidnightConfig;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.List;

public class Config extends MidnightConfig {
    public static final String GENERAL = "general";
    @Entry(category = GENERAL, idMode = 1, width = 1000)
    public static List<Identifier> tiltedSlabs = Lists.newArrayList(Identifier.ofVanilla("smooth_stone_slab"));

    public static boolean isTilted(Block block) {
        var id = Registries.BLOCK.getId(block);
        return tiltedSlabs.contains(id);
    }
}
