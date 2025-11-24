package dev.micalobia.fullslabs.fabric.client;

import dev.micalobia.fullslabs.block.VerticalSlabBlock;
import dev.micalobia.fullslabs.client.FullSlabsClient;
import dev.micalobia.fullslabs.config.Controls;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.ArrayList;
import java.util.List;

public final class FullSlabsFabricClient implements ClientModInitializer {
    private static boolean clientStarted = false;
    private static final List<VerticalSlabBlock> pending = new ArrayList<>();

    @Override
    public void onInitializeClient() {
        FullSlabsClient.init();
        ModelLoadingPlugin.register(new VerticalModelLoadingPlugin());
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            clientStarted = true;
            VerticalSlabBlock.MAP_VIEW.values().forEach(FullSlabsFabricClient::renderLayer);
            pending.forEach(FullSlabsFabricClient::renderLayer);
            pending.clear();
        });
        // I'm not sure if this is necessary, but it's difficult to test load order
        RegistryEntryAddedCallback.event(BuiltInRegistries.BLOCK).register(((i, identifier, block) -> {
            if (!(block instanceof VerticalSlabBlock slab)) return;
            if (clientStarted) renderLayer(slab);
            else pending.add(slab);
        }));
        Controls.clientInit();
    }

    private static void renderLayer(VerticalSlabBlock slab) {
        BlockRenderLayerMap.putBlock(slab, ItemBlockRenderTypes.getChunkRenderType(slab.parent.defaultBlockState()));
    }
}

