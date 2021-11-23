package dev.micalobia.full_slabs;

import dev.micalobia.full_slabs.util.Utility;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;


@Environment(EnvType.SERVER)
public class FullSlabsModServer implements DedicatedServerModInitializer {
	@Override
	public void onInitializeServer() {
		ServerPlayNetworking.registerGlobalReceiver(FullSlabsMod.id("toggle_vertical"), ((server, player, handler, buf, responseSender) -> {
			Utility.setVerticalEnabled(player.getUuid(), buf.readBoolean());
		}));
	}
}
