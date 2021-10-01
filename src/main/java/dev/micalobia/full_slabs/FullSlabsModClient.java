package dev.micalobia.full_slabs;

import dev.micalobia.full_slabs.client.render.OutlineRenderer;
import dev.micalobia.full_slabs.client.render.model.FullSlabModelProvider;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.render.RenderLayer;

public class FullSlabsModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModelLoadingRegistry.INSTANCE.registerResourceProvider(rm -> new FullSlabModelProvider());
		BlockRenderLayerMap.INSTANCE.putBlock(FullSlabsMod.EXTRA_SLAB_BLOCK, RenderLayer.getCutout());
		OutlineRenderer.init();
		if(FabricLoader.getInstance().isModLoaded("malilib")) {
			OverlayRenderer.init();
		}
	}
}
