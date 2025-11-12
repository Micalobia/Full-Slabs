package dev.micalobia.fullslabs.config;


import com.google.common.collect.Lists;
import eu.midnightdust.lib.config.MidnightConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class Config extends MidnightConfig {
    public static final String GENERAL = "general";
    public static final String OVERLAY = "overlay";
    @Entry(category = GENERAL, idMode = 1, width = 1000)
    public static List<ResourceLocation> tiltedSlabs = Lists.newArrayList(
            ResourceLocation.withDefaultNamespace("smooth_stone_slab"),
            ResourceLocation.fromNamespaceAndPath("mo_glass", "glass_slab"),
            ResourceLocation.fromNamespaceAndPath("mo_glass", "white_stained_glass_slab"),
            ResourceLocation.fromNamespaceAndPath("mo_glass", "orange_stained_glass_slab"),
            ResourceLocation.fromNamespaceAndPath("mo_glass", "magenta_stained_glass_slab"),
            ResourceLocation.fromNamespaceAndPath("mo_glass", "light_blue_stained_glass_slab"),
            ResourceLocation.fromNamespaceAndPath("mo_glass", "yellow_stained_glass_slab"),
            ResourceLocation.fromNamespaceAndPath("mo_glass", "lime_stained_glass_slab"),
            ResourceLocation.fromNamespaceAndPath("mo_glass", "pink_stained_glass_slab"),
            ResourceLocation.fromNamespaceAndPath("mo_glass", "gray_stained_glass_slab"),
            ResourceLocation.fromNamespaceAndPath("mo_glass", "light_gray_stained_glass_slab"),
            ResourceLocation.fromNamespaceAndPath("mo_glass", "cyan_stained_glass_slab"),
            ResourceLocation.fromNamespaceAndPath("mo_glass", "purple_stained_glass_slab"),
            ResourceLocation.fromNamespaceAndPath("mo_glass", "blue_stained_glass_slab"),
            ResourceLocation.fromNamespaceAndPath("mo_glass", "brown_stained_glass_slab"),
            ResourceLocation.fromNamespaceAndPath("mo_glass", "green_stained_glass_slab"),
            ResourceLocation.fromNamespaceAndPath("mo_glass", "red_stained_glass_slab"),
            ResourceLocation.fromNamespaceAndPath("mo_glass", "black_stained_glass_slab"),
            ResourceLocation.fromNamespaceAndPath("mo_glass", "tinted_glass_slab")
    );
    @Entry(category = OVERLAY, min = 0, max = 255, isSlider = true)
    public static int edgeOpacity = 255;
    @Entry(category = OVERLAY, isColor = true)
    public static String edgeColor = "#FFFFFF";
    @Entry(category = OVERLAY, min = 0, max = 255, isSlider = true)
    public static int fillOpacity = 63;
    @Entry(category = OVERLAY, isColor = true)
    public static String fillColor = "#007FFF";

    private static int cachedEdgeColor = 0xFFFFFFFF;
    private static int cachedFillColor = 0x3F007FFF;

    public static boolean isTilted(Block block) {
        var id = BuiltInRegistries.BLOCK.getKey(block);
        return tiltedSlabs.contains(id);
    }

    public static int edgeColor() {return cachedEdgeColor;}

    public static int fillColor() {return cachedFillColor;}

    @Override
    public void writeChanges() {
        super.writeChanges();
        cachedEdgeColor = parseColor(edgeOpacity, edgeColor);
        cachedFillColor = parseColor(fillOpacity, fillColor);
    }

    @Override
    public void loadValuesFromJson() {
        super.loadValuesFromJson();
        cachedEdgeColor = parseColor(edgeOpacity, edgeColor);
        cachedFillColor = parseColor(fillOpacity, fillColor);
    }

    private int parseColor(int opacity, String color) {
        var hex = color.replaceAll("[^0-9A-Fa-f]", "");
        if (hex.length() > 6) hex = hex.substring(0, 6);
        else if (hex.length() == 3) hex = String.valueOf(hex.charAt(0)) + hex.charAt(0) +
                hex.charAt(1) + hex.charAt(1) +
                hex.charAt(2) + hex.charAt(2);
        else if (hex.length() < 6) return 0xFFFFFFFF;
        var r = Integer.parseInt(hex.substring(0, 2), 16);
        var g = Integer.parseInt(hex.substring(2, 4), 16);
        var b = Integer.parseInt(hex.substring(4, 6), 16);
        return opacity << 24 | r << 16 | g << 8 | b;
    }
}
