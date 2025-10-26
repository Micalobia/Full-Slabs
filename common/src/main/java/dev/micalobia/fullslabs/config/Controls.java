package dev.micalobia.fullslabs.config;

import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import dev.micalobia.fullslabs.FullSlabs;
import dev.micalobia.fullslabs.util.SlabPlacement.Mode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Controls {
    private static final Map<UUID, Mode> modeMap = new HashMap<>();
    private static final KeyBinding.Category MAIN = KeyBinding.Category.create(FullSlabs.id("main"));
    private static KeyBinding cycleMode;
    private static KeyBinding toggleOverlay;
    private static boolean overlayActive = true;

    public static Mode getPlacementMode(UUID player) {
        return modeMap.computeIfAbsent(player, uuid -> Mode.HYBRID);
    }

    private static void cyclePlacementMode(UUID player) {
        modeMap.put(player, getPlacementMode(player).next());
    }

    private static void setPlacementMode(UUID player, Mode mode) {
        modeMap.put(player, mode);
    }

    private static void receivePlacementMode(Mode mode, NetworkManager.PacketContext context) {
        setPlacementMode(context.getPlayer().getUuid(), mode);
    }

    public static boolean isOverlayActive() {
        return overlayActive;
    }

    public static void toggleOverlayActive() {
        overlayActive = !overlayActive;
    }

    private static KeyBinding register(String id, int code) {
        var binding = new KeyBinding("key.fullslabs.%s".formatted(id), code, MAIN);
        KeyMappingRegistry.register(binding);
        return binding;
    }

    public static void serverInit() {
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, Mode.PACKET_TYPE, Mode.PACKET_CODEC, Controls::receivePlacementMode);
    }

    public static void clientInit() {
        toggleOverlay = register("toggle_overlay", InputUtil.UNKNOWN_KEY.getCode());
        cycleMode = register("cycle_mode", InputUtil.GLFW_KEY_V);
        ClientTickEvent.CLIENT_POST.register(Controls::onClientTick);
    }

    private static void onClientTick(MinecraftClient client) {
        if (client.player == null) return;
        var uuid = client.player.getUuid();

        var sendVerticalPacket = false;
        while (toggleOverlay.wasPressed()) toggleOverlayActive();
        while (cycleMode.wasPressed()) {
            cyclePlacementMode(uuid);
            sendVerticalPacket = true;
        }
        if (sendVerticalPacket) NetworkManager.sendToServer(getPlacementMode(uuid));
    }
}
