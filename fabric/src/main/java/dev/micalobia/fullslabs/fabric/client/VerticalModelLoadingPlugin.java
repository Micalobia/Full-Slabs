package dev.micalobia.fullslabs.fabric.client;

import dev.micalobia.fullslabs.VerticalSlabBlock;
import dev.micalobia.fullslabs.client.models.VerticalSlabModel;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.render.model.SimpleBlockStateModel;
import net.minecraft.client.render.model.json.ModelVariant;
import net.minecraft.registry.Registries;

public final class VerticalModelLoadingPlugin implements ModelLoadingPlugin {
    @Override
    public void initialize(Context context) {
        Registries.BLOCK.stream().filter(block -> block instanceof VerticalSlabBlock).forEach(block -> context.registerBlockStateResolver(block, ctx -> block.getStateManager().getStates().forEach(state -> {
            var id = VerticalSlabModel.makeModelId(state);
            var variant = new ModelVariant(id, ModelVariant.ModelState.DEFAULT);
            var unbaked = new SimpleBlockStateModel.Unbaked(variant);
            ctx.setModel(state, unbaked.cached());
        })));
        context.modifyBlockModelOnLoad().register((model, ctx) -> {
            var state = ctx.state();
            if (!(state.getBlock() instanceof VerticalSlabBlock slab)) return model;
            return new VerticalSlabModel();
        });
    }
}
