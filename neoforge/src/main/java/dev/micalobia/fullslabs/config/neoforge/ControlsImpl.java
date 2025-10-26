package dev.micalobia.fullslabs.config.neoforge;

import dev.architectury.event.events.client.ClientTickEvent;
import dev.micalobia.fullslabs.FullSlabs;
import dev.micalobia.fullslabs.config.Controls;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.KeyBinding.Category;
import net.minecraft.client.util.InputUtil;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber(value = Dist.CLIENT, modid = FullSlabs.MODID)
public class ControlsImpl {
    public static Category createMain() {
        return new Category(FullSlabs.id("main"));
    }

    public static void clientInit() {
        // no-op because of the event
    }

    @SubscribeEvent
    public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        Controls.toggleOverlay = new KeyBinding("key.fullslabs.%s".formatted("toggle_overlay"), InputUtil.UNKNOWN_KEY.getCode(), Controls.MAIN);
        Controls.cycleMode = new KeyBinding("key.fullslabs.%s".formatted("cycle_mode"), InputUtil.GLFW_KEY_V, Controls.MAIN);
        event.registerCategory(Controls.MAIN);
        event.register(Controls.toggleOverlay);
        event.register(Controls.cycleMode);
        ClientTickEvent.CLIENT_POST.register(Controls::onClientTick);
    }
}
