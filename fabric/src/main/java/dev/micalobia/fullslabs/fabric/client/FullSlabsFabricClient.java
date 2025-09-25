package dev.micalobia.fullslabs.fabric.client;

import dev.micalobia.fullslabs.VerticalSlabBlock;
import dev.micalobia.fullslabs.client.BlockFaceOverlay;
import dev.micalobia.fullslabs.util.Utility;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.hit.BlockHitResult;

public final class FullSlabsFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModelLoadingPlugin.register(new VerticalModelLoadingPlugin());
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> Registries.BLOCK.stream().filter(block -> block instanceof VerticalSlabBlock).map(block -> (VerticalSlabBlock) block).forEach(FullSlabsFabricClient::renderLayer));
        RegistryEntryAddedCallback.event(Registries.BLOCK).register(((i, identifier, block) -> {
            if (!(block instanceof VerticalSlabBlock slab)) return;
            renderLayer(slab);
        }));
        WorldRenderEvents.BLOCK_OUTLINE.register((ctx, boc) -> {
            var mc = MinecraftClient.getInstance();
            if (!(mc.crosshairTarget instanceof BlockHitResult bhr)) return true;
            if (!(boc.entity() instanceof PlayerEntity player)) return true;
            if (!player.isHolding(Utility::isSlabWithVertical)) return true;
            var world = ctx.world();
            var pos = bhr.getBlockPos();
            var hit = bhr.getPos();
            var face = bhr.getSide();
            var state = world.getBlockState(pos);
            var camera = ctx.camera();
            BlockFaceOverlay.renderFaceOverlay(camera, world, pos, state, face, hit);
            return true;
        });
    }

    private static void renderLayer(VerticalSlabBlock slab) {
        BlockRenderLayerMap.putBlock(slab, RenderLayers.getBlockLayer(slab.parent.getDefaultState()));
    }
}
