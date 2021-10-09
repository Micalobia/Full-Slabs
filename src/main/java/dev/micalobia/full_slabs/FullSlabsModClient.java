package dev.micalobia.full_slabs;

import dev.micalobia.full_slabs.client.render.OutlineRenderer;
import dev.micalobia.full_slabs.client.render.model.FullSlabModelProvider;
import dev.micalobia.full_slabs.util.Utility;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil.Type;
import org.lwjgl.glfw.GLFW;

public class FullSlabsModClient implements ClientModInitializer {
	private static KeyBinding toggleWidget;

	@Override
	public void onInitializeClient() {
		ModelLoadingRegistry.INSTANCE.registerResourceProvider(rm -> new FullSlabModelProvider());
		OutlineRenderer.init();
		if(FabricLoader.getInstance().isModLoaded("malilib")) {
			OverlayRenderer.init();
			toggleWidget = KeyBindingHelper.registerKeyBinding(new KeyBinding(
					"key.full_slabs.toggle_widget",
					Type.KEYSYM,
					GLFW.GLFW_KEY_V,
					"category.full_slabs.full_slabs"
			));
			ClientTickEvents.END_CLIENT_TICK.register(client -> {
				while(toggleWidget.wasPressed())
					Utility.toggleShowWidget();
			});
		}
	}
}
