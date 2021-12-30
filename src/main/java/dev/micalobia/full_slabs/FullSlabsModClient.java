package dev.micalobia.full_slabs;

import dev.micalobia.full_slabs.client.render.OutlineRenderer;
import dev.micalobia.full_slabs.client.render.OverlayRenderer;
import dev.micalobia.full_slabs.client.render.model.FullSlabModelProvider;
import dev.micalobia.full_slabs.config.CustomControls;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.loader.api.FabricLoader;

public class FullSlabsModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModelLoadingRegistry.INSTANCE.registerResourceProvider(rm -> new FullSlabModelProvider());
		OutlineRenderer.init();
		if(FabricLoader.getInstance().isModLoaded("malilib"))
			OverlayRenderer.init();
		CustomControls.clientInit();
	}
}
