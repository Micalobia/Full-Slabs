package dev.micalobia.full_slabs;

import dev.micalobia.full_slabs.client.render.OutlineRenderer;
import dev.micalobia.full_slabs.client.render.OverlayRenderer;
import dev.micalobia.full_slabs.client.render.model.FullSlabModelProvider;
import dev.micalobia.full_slabs.config.CustomControls;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;

public class FullSlabsModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModelLoadingRegistry.INSTANCE.registerResourceProvider(rm -> new FullSlabModelProvider());
		OutlineRenderer.init();
		OverlayRenderer.init();
		CustomControls.clientInit();
	}
}
