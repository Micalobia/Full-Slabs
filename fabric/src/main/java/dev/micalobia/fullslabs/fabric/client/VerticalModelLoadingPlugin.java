package dev.micalobia.fullslabs.fabric.client;

import dev.micalobia.fullslabs.SlabRegistry;
import dev.micalobia.fullslabs.block.VerticalSlabBlock;
import dev.micalobia.fullslabs.client.models.MixedSlabModel;
import dev.micalobia.fullslabs.client.models.VerticalSlabModel;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;

public final class VerticalModelLoadingPlugin implements ModelLoadingPlugin {
    @Override
    public void initialize(Context context) {
        VerticalSlabBlock.MAP_VIEW.values().forEach(block -> context.registerBlockStateResolver(block, ctx -> block.getStateDefinition().getPossibleStates().forEach(state ->
                ctx.setModel(state, VerticalSlabModel.INSTANCE)
        )));
        context.registerBlockStateResolver(SlabRegistry.MIXED_SLAB.get(), ctx -> ctx.block().getStateDefinition().getPossibleStates().forEach(state ->
                ctx.setModel(state, MixedSlabModel.INSTANCE)
        ));
    }
}
