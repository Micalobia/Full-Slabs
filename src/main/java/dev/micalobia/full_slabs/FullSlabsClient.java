package dev.micalobia.full_slabs;

import dev.micalobia.full_slabs.block.Blocks;
import dev.micalobia.full_slabs.client.render.model.SlabModelProvider;
import dev.micalobia.full_slabs.util.LinkedSlabs;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.minecraft.client.render.RenderLayer;

@Environment(EnvType.CLIENT)
public class FullSlabsClient implements ClientModInitializer {
	public void onInitializeClient() {
		ModelLoadingRegistry.INSTANCE.registerVariantProvider(vm -> new SlabModelProvider());
		ModelLoadingRegistry.INSTANCE.registerResourceProvider(rm -> new SlabModelProvider());
		LinkedSlabs.correctRenderLayers();
		BlockRenderLayerMap map = BlockRenderLayerMap.INSTANCE;
		map.putBlock(Blocks.FULL_SLAB_BLOCK, RenderLayer.getTranslucent());
	}
}
