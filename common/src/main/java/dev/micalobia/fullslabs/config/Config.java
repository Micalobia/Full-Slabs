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
    public static List<Identifier> tiltedSlabs = Lists.newArrayList(
            Identifier.ofVanilla("smooth_stone_slab"),
            Identifier.of("mo_glass", "glass_slab"),
            Identifier.of("mo_glass", "white_stained_glass_slab"),
            Identifier.of("mo_glass", "orange_stained_glass_slab"),
            Identifier.of("mo_glass", "magenta_stained_glass_slab"),
            Identifier.of("mo_glass", "light_blue_stained_glass_slab"),
            Identifier.of("mo_glass", "yellow_stained_glass_slab"),
            Identifier.of("mo_glass", "lime_stained_glass_slab"),
            Identifier.of("mo_glass", "pink_stained_glass_slab"),
            Identifier.of("mo_glass", "gray_stained_glass_slab"),
            Identifier.of("mo_glass", "light_gray_stained_glass_slab"),
            Identifier.of("mo_glass", "cyan_stained_glass_slab"),
            Identifier.of("mo_glass", "purple_stained_glass_slab"),
            Identifier.of("mo_glass", "blue_stained_glass_slab"),
            Identifier.of("mo_glass", "brown_stained_glass_slab"),
            Identifier.of("mo_glass", "green_stained_glass_slab"),
            Identifier.of("mo_glass", "red_stained_glass_slab"),
            Identifier.of("mo_glass", "black_stained_glass_slab"),
            Identifier.of("mo_glass", "tinted_glass_slab")
    );

    public static boolean isTilted(Block block) {
        var id = Registries.BLOCK.getId(block);
        return tiltedSlabs.contains(id);
    }
}
