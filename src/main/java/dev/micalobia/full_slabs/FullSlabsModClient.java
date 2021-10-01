package dev.micalobia.full_slabs;

import dev.micalobia.full_slabs.client.render.OutlineRenderer;
import dev.micalobia.full_slabs.client.render.model.FullSlabModelProvider;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.loader.api.FabricLoader;

public class FullSlabsModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModelLoadingRegistry.INSTANCE.registerResourceProvider(rm -> new FullSlabModelProvider());
		WorldRenderEvents.BLOCK_OUTLINE.register(OutlineRenderer::renderSlabOutline);
		WorldRenderEvents.BLOCK_OUTLINE.register(OutlineRenderer::renderFullSlabOutline);
		if(FabricLoader.getInstance().isModLoaded("malilib")) {
			OverlayRenderer.init();
		}
	}
}
