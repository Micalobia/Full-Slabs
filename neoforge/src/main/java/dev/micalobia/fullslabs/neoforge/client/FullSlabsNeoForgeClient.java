package dev.micalobia.fullslabs.neoforge.client;

import dev.micalobia.fullslabs.FullSlabs;
import dev.micalobia.fullslabs.SlabRegistry;
import dev.micalobia.fullslabs.block.MixedSlabBlock;
import dev.micalobia.fullslabs.block.VerticalSlabBlock;
import dev.micalobia.fullslabs.client.BlockFaceOverlay;
import dev.micalobia.fullslabs.client.FullSlabsClient;
import dev.micalobia.fullslabs.client.models.MixedSlabModel;
import dev.micalobia.fullslabs.client.models.VerticalSlabModel;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.BlockStateModel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import net.neoforged.neoforge.client.model.standalone.UnbakedStandaloneModel;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(value = Dist.CLIENT, modid = FullSlabs.MODID)
public final class FullSlabsNeoForgeClient {
    public static final Map<BlockState, StandaloneModelKey<BlockStateModel>> keys = new HashMap<>();

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        FullSlabsClient.init();
    }

    @SubscribeEvent
    public static void registerStandalones(ModelEvent.RegisterStandalone event) {
        keys.clear();
        for (var vertical : VerticalSlabBlock.MAP_VIEW.values()) {
            for (var state : vertical.getStateManager().getStates()) {
                var id = VerticalSlabModel.makeModelId(state);
                var key = new StandaloneModelKey<BlockStateModel>(id::toString);
                event.register(key, new UnbakedGroupedWrapper(VerticalSlabModel.INSTANCE, state));
                keys.put(state, key);
            }
        }
        var mixed = SlabRegistry.MIXED_SLAB.get();
        for (var state : mixed.getStateManager().getStates()) {
            var id = FullSlabs.id("block/mixed_slab/%s".formatted(state.get(MixedSlabBlock.TYPE).asString()));
            var key = new StandaloneModelKey<BlockStateModel>(id::toString);
            event.register(key, new UnbakedGroupedWrapper(MixedSlabModel.INSTANCE, state));
            keys.put(state, key);
        }
    }

    @SubscribeEvent
    public static void mapStandalones(ModelEvent.ModifyBakingResult event) {
        var result = event.getBakingResult();
        var standaloneModels = result.standaloneModels();
        var blockStateModels = event.getBakingResult().blockStateModels();
        for (var vertical : VerticalSlabBlock.MAP_VIEW.values()) {
            for (var state : vertical.getStateManager().getStates()) {
                var key = keys.get(state);
                var model = standaloneModels.get(key);
                blockStateModels.put(state, model);
            }
        }
        var mixed = SlabRegistry.MIXED_SLAB.get();
        for (var state : mixed.getStateManager().getStates()) {
            var key = keys.get(state);
            var model = standaloneModels.get(key);
            blockStateModels.put(state, model);
        }
    }

    @SubscribeEvent
    public static void renderOverlay(RenderLevelStageEvent.AfterEntities event) {
        var client = MinecraftClient.getInstance();
        if (client.options.hudHidden) return;
        BlockFaceOverlay.renderFaceOverlay(event.getLevelRenderState());
    }

    public static class UnbakedGroupedWrapper implements UnbakedStandaloneModel<BlockStateModel> {
        private final BlockStateModel.UnbakedGrouped model;
        private final BlockState state;

        public UnbakedGroupedWrapper(BlockStateModel.UnbakedGrouped model, BlockState state) {
            this.model = model;
            this.state = state;
        }

        @Override
        @NotNull
        public BlockStateModel bake(@NotNull Baker baker) {
            return this.model.bake(state, baker);
        }

        @Override
        public void resolve(Resolver resolver) {
            this.model.resolve(resolver);
        }
    }
}
