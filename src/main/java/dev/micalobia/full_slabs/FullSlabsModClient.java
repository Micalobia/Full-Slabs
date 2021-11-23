package dev.micalobia.full_slabs;

import dev.micalobia.full_slabs.client.render.OutlineRenderer;
import dev.micalobia.full_slabs.client.render.model.FullSlabModelProvider;
import dev.micalobia.full_slabs.util.Utility;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.InputUtil.Type;
import net.minecraft.network.PacketByteBuf;
import org.lwjgl.glfw.GLFW;

public class FullSlabsModClient implements ClientModInitializer {
	private static KeyBinding toggleWidget;
	private static KeyBinding toggleVertical;

	@Override
	public void onInitializeClient() {
		ModelLoadingRegistry.INSTANCE.registerResourceProvider(rm -> new FullSlabModelProvider());
		OutlineRenderer.init();
		toggleVertical = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.full_slabs.toggle_vertical",
				InputUtil.UNKNOWN_KEY.getCode(),
				"category.full_slabs.full_slabs"
		));
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			boolean wasPressed = false;
			while(toggleVertical.wasPressed()) {
				if(client.player == null) break;
				Utility.toggleVerticalEnabled(client.player.getUuid());
				wasPressed = true;
			}
			if(wasPressed) {
				PacketByteBuf buf = PacketByteBufs.create();
				buf.writeBoolean(Utility.getVerticalEnabled(client.player.getUuid()));
				ClientPlayNetworking.send(FullSlabsMod.id("toggle_vertical"), buf);
			}
		});
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
