package dev.micalobia.full_slabs.config;

import dev.micalobia.full_slabs.FullSlabsMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.InputUtil.Type;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CustomControls {
	private static final Map<UUID, Boolean> verticalEnabledMap = new HashMap<>();
	private static boolean showWidget = true;
	private static KeyBinding toggleWidget;
	private static KeyBinding toggleVertical;

	@Environment(EnvType.CLIENT)
	public static boolean getShowWidget() {
		return showWidget;
	}

	@Environment(EnvType.CLIENT)
	public static void toggleShowWidget() {
		showWidget = !showWidget;
	}

	public static boolean getVerticalEnabled(UUID player) {
		return verticalEnabledMap.computeIfAbsent(player, x -> true);
	}

	public static void toggleVerticalEnabled(UUID player) {
		verticalEnabledMap.put(player, !verticalEnabledMap.get(player));
	}

	public static void setVerticalEnabled(UUID player, boolean value) {
		verticalEnabledMap.put(player, value);
	}

	@Environment(EnvType.CLIENT)
	private static void handleVerticalToggle(MinecraftClient client) {
		boolean wasPressed = false;
		while(toggleVertical.wasPressed()) {
			if(client.player == null) break;
			toggleVerticalEnabled(client.player.getUuid());
			wasPressed = true;
		}
		if(wasPressed) {
			PacketByteBuf buf = PacketByteBufs.create();
			buf.writeBoolean(getVerticalEnabled(client.player.getUuid()));
			ClientPlayNetworking.send(FullSlabsMod.id("toggle_vertical"), buf);
		}
	}

	@Environment(EnvType.CLIENT)
	private static void handleWidgetToggle(MinecraftClient client) {
		while(toggleWidget.wasPressed())
			toggleShowWidget();
	}

	@SuppressWarnings("SameParameterValue")
	private static KeyBinding register(String nameKey, String categoryKey, int code) {
		return KeyBindingHelper.registerKeyBinding(new KeyBinding(nameKey, code, categoryKey));
	}

	private static KeyBinding register(String nameKey, String categoryKey, int code, Type type) {
		return KeyBindingHelper.registerKeyBinding(new KeyBinding(nameKey, type, code, categoryKey));
	}

	@Environment(EnvType.CLIENT)
	public static void clientInit() {
		toggleVertical = register("key.full_slabs.toggle_vertical", "category.full_slabs.full_slabs", InputUtil.UNKNOWN_KEY.getCode());
		toggleWidget = register("key.full_slabs.toggle_widget", "category.full_slabs.full_slabs", GLFW.GLFW_KEY_V);
		ClientTickEvents.END_CLIENT_TICK.register(CustomControls::handleVerticalToggle);
		ClientTickEvents.END_CLIENT_TICK.register(CustomControls::handleWidgetToggle);
	}

	@Environment(EnvType.SERVER)
	public static void serverInit() {
		ServerPlayNetworking.registerGlobalReceiver(FullSlabsMod.id("toggle_vertical"), (CustomControls::receiveVerticalToggle));
	}

	@Environment(EnvType.SERVER)
	private static void receiveVerticalToggle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
		setVerticalEnabled(player.getUuid(), buf.readBoolean());
	}
}
