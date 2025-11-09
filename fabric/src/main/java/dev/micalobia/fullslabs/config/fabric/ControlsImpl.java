package dev.micalobia.fullslabs.config.fabric;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import dev.micalobia.fullslabs.FullSlabs;
import dev.micalobia.fullslabs.config.Controls;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyMapping.Category;

@SuppressWarnings("unused")
public class ControlsImpl {
    public static void clientInit() {
        Controls.MAIN = Category.register(FullSlabs.id("main"));
        Controls.toggleOverlay = register("toggle_overlay", InputConstants.UNKNOWN.getValue());
        Controls.cycleMode = register("cycle_mode", InputConstants.KEY_V);
        ClientTickEvent.CLIENT_POST.register(Controls::onClientTick);
    }

    private static KeyMapping register(String id, int code) {
        var binding = new KeyMapping("key.fullslabs.%s".formatted(id), code, Controls.MAIN);
        KeyMappingRegistry.register(binding);
        return binding;
    }
}
