package dev.micalobia.fullslabs.config.fabric;

import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import dev.micalobia.fullslabs.FullSlabs;
import dev.micalobia.fullslabs.config.Controls;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.KeyBinding.Category;
import net.minecraft.client.util.InputUtil;

public class ControlsImpl {
    public static Category createMain() {
        return Category.create(FullSlabs.id("main"));
    }

    public static void clientInit() {
        Controls.toggleOverlay = register("toggle_overlay", InputUtil.UNKNOWN_KEY.getCode());
        Controls.cycleMode = register("cycle_mode", InputUtil.GLFW_KEY_V);
        ClientTickEvent.CLIENT_POST.register(Controls::onClientTick);
    }

    private static KeyBinding register(String id, int code) {
        var binding = new KeyBinding("key.fullslabs.%s".formatted(id), code, Controls.MAIN);
        KeyMappingRegistry.register(binding);
        return binding;
    }
}
