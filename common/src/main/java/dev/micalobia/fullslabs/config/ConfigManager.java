package dev.micalobia.fullslabs.config;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.ListOption;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import dev.isxander.yacl3.platform.YACLPlatform;
import dev.micalobia.fullslabs.FullSlabs;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {
    private static final Path FILE = YACLPlatform.getConfigDir().resolve(FullSlabs.MODID + ".json5");

    private static final ConfigClassHandler<Config> HANDLER = ConfigClassHandler.createBuilder(Config.class)
            .id(FullSlabs.id("config"))
            .serializer(cfg -> GsonConfigSerializerBuilder.create(cfg)
                    .setPath(FILE)
                    .setJson5(true)
                    .build())
            .build();

    private ConfigManager() {
    }

    public static void init() {
        HANDLER.load();
    }

    public static Config get() {
        return HANDLER.instance();
    }

    public static void save() {
        HANDLER.save();
    }

    public static Screen createScreen(Screen parent) {
        var cfg = HANDLER.instance();

        var tilted = ListOption.<String>createBuilder()
                .name(Text.translatable("fullslabs.config.general.tilted"))
                .binding(List.of("minecraft:smooth_stone_slab"), () -> cfg.tiltedSlabs, list -> cfg.tiltedSlabs = new ArrayList<>(list))
                .controller(StringControllerBuilder::create)
                .initial("")
                .build();
        return YetAnotherConfigLib.createBuilder()
                .title(Text.translatable("fullslabs.config.title"))
                .save(HANDLER::save)
                .category(() -> ConfigCategory.createBuilder()
                        .name(Text.translatable("fullslabs.config.title.general"))
                        .option(tilted)
                        .build()
                ).build().generateScreen(parent);
    }
}
