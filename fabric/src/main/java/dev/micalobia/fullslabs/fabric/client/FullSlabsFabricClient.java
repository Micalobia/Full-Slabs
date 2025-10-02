package dev.micalobia.fullslabs.fabric.client;

import dev.micalobia.fullslabs.VerticalSlabBlock;
import dev.micalobia.fullslabs.client.BlockFaceOverlay;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.registry.Registries;

public final class FullSlabsFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModelLoadingPlugin.register(new VerticalModelLoadingPlugin());
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> VerticalSlabBlock.MAP_VIEW.values().forEach(FullSlabsFabricClient::renderLayer));
        RegistryEntryAddedCallback.event(Registries.BLOCK).register(((i, identifier, block) -> {
            if (!(block instanceof VerticalSlabBlock slab)) return;
            renderLayer(slab);
        }));
        WorldRenderEvents.BLOCK_OUTLINE.register((ctx, boc) -> {
            BlockFaceOverlay.renderFaceOverlay(ctx.camera());
            return true;
        });
    }

    private static void renderLayer(VerticalSlabBlock slab) {
        BlockRenderLayerMap.putBlock(slab, RenderLayers.getBlockLayer(slab.parent.getDefaultState()));
    }
}
