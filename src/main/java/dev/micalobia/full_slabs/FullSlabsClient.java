package dev.micalobia.full_slabs;

import dev.micalobia.full_slabs.client.render.model.SlabModelProvider;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;

@Environment(EnvType.CLIENT)
public class FullSlabsClient implements ClientModInitializer {
	public void onInitializeClient() {
		ModelLoadingRegistry.INSTANCE.registerVariantProvider(vm -> new SlabModelProvider());
		ModelLoadingRegistry.INSTANCE.registerResourceProvider(rm -> new SlabModelProvider());
	}
}
