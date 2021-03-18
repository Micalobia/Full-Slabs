package dev.micalobia.full_slabs;

import dev.micalobia.full_slabs.block.Blocks;
import dev.micalobia.full_slabs.block.VerticalSlabBlock;
import dev.micalobia.full_slabs.client.render.model.SlabModelProvider;
import dev.micalobia.full_slabs.util.LinkedSlabs;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.block.Block;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

@Environment(EnvType.CLIENT)
public class FullSlabsClient implements ClientModInitializer {
	private static BlockRenderLayerMap map;

	private static void syncRenderLayer(int i, Identifier identifier, Block block) {
		syncRenderLayer(block);
	}

	private static void syncRenderLayer(Block block) {
		if(!(block instanceof VerticalSlabBlock)) return;
		RenderLayer layer = RenderLayers.getBlockLayer(LinkedSlabs.horizontal(block).getDefaultState());
		map.putBlock(block, RenderLayer.getTranslucent()); // TODO: Figure out why using layer doesn't work unless i'm running from intellij
	}

	public void onInitializeClient() {
		ModelLoadingRegistry.INSTANCE.registerVariantProvider(vm -> new SlabModelProvider());
		ModelLoadingRegistry.INSTANCE.registerResourceProvider(rm -> new SlabModelProvider());
		map = BlockRenderLayerMap.INSTANCE;
		Registry.BLOCK.forEach(FullSlabsClient::syncRenderLayer);
		RegistryEntryAddedCallback.event(Registry.BLOCK).register(FullSlabsClient::syncRenderLayer);
		BlockRenderLayerMap map = BlockRenderLayerMap.INSTANCE;
		map.putBlock(Blocks.FULL_SLAB_BLOCK, RenderLayer.getTranslucent());
	}


}
