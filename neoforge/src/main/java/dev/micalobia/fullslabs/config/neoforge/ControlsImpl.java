package dev.micalobia.fullslabs.config.neoforge;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.micalobia.fullslabs.FullSlabs;
import dev.micalobia.fullslabs.config.Controls;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyMapping.Category;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber(value = Dist.CLIENT, modid = FullSlabs.MODID)
public class ControlsImpl {
    @SuppressWarnings("unused")
    public static void clientInit() {
        // no-op because of the event
    }

    @SubscribeEvent
    public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        Controls.MAIN = new Category(FullSlabs.id("main"));
        Controls.toggleOverlay = new KeyMapping("key.fullslabs.%s".formatted("toggle_overlay"), InputConstants.UNKNOWN.getValue(), Controls.MAIN);
        Controls.cycleMode = new KeyMapping("key.fullslabs.%s".formatted("cycle_mode"), InputConstants.KEY_V, Controls.MAIN);
        event.registerCategory(Controls.MAIN);
        event.register(Controls.toggleOverlay);
        event.register(Controls.cycleMode);
        ClientTickEvent.CLIENT_POST.register(Controls::onClientTick);
    }
}
