package dev.micalobia.fullslabs.config;

import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.architectury.networking.NetworkManager;
import dev.micalobia.fullslabs.util.SlabPlacement.Mode;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyMapping.Category;
import net.minecraft.client.Minecraft;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Controls {
    private static final Map<UUID, Mode> modeMap = new HashMap<>();
    private static boolean overlayActive = true;
    public static Category MAIN;
    public static KeyMapping cycleMode;
    public static KeyMapping toggleOverlay;

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
        setPlacementMode(context.getPlayer().getUUID(), mode);
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

    public static void onClientTick(Minecraft client) {
        if (client.player == null) return;
        var uuid = client.player.getUUID();

        var sendVerticalPacket = false;
        while (toggleOverlay.consumeClick()) toggleOverlayActive();
        while (cycleMode.consumeClick()) {
            cyclePlacementMode(uuid);
            sendVerticalPacket = true;
        }
        if (sendVerticalPacket) NetworkManager.sendToServer(getPlacementMode(uuid));
    }
}
