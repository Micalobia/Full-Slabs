package dev.micalobia.fullslabs.config;

import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.architectury.networking.NetworkManager;
import dev.micalobia.fullslabs.util.SlabPlacement.Mode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.KeyBinding.Category;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Controls {
    private static final Map<UUID, Mode> modeMap = new HashMap<>();
    private static boolean overlayActive = true;
    public static Category MAIN;
    public static KeyBinding cycleMode;
    public static KeyBinding toggleOverlay;

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

    public static void serverInit() {
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, Mode.PACKET_TYPE, Mode.PACKET_CODEC, Controls::receivePlacementMode);
    }

    @ExpectPlatform
    public static void clientInit() {
        throw new AssertionError();
    }

    public static void onClientTick(MinecraftClient client) {
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
