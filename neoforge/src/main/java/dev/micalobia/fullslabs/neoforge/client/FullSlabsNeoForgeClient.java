package dev.micalobia.fullslabs.neoforge.client;

import dev.micalobia.fullslabs.FullSlabs;
import dev.micalobia.fullslabs.block.VerticalSlabBlock;
import dev.micalobia.fullslabs.client.BlockFaceOverlay;
import dev.micalobia.fullslabs.client.FullSlabsClient;
import dev.micalobia.fullslabs.client.models.VerticalSlabModel;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.BlockStateModel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
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

    public static void init() {
        FullSlabsClient.init();
    }

    @SubscribeEvent
    public static void registerVerticalStandalones(ModelEvent.RegisterStandalone event) {
        keys.clear();
        for (var vertical : VerticalSlabBlock.MAP_VIEW.values()) {
            for (var state : vertical.getStateManager().getStates()) {
                var id = VerticalSlabModel.makeModelId(state);
                var key = new StandaloneModelKey<BlockStateModel>(id::toString);
                event.register(key, new VerticalWrapper(state));
                keys.put(state, key);
            }
        }
    }

    @SubscribeEvent
    public static void mapVerticalStandalones(ModelEvent.ModifyBakingResult event) {
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
    }

    @SubscribeEvent
    public static void renderOverlay(RenderLevelStageEvent.AfterBlockEntities event) {
        var client = MinecraftClient.getInstance();
        if (client.options.hudHidden) return;
        BlockFaceOverlay.renderFaceOverlay(event.getCamera());
    }

    public static class VerticalWrapper implements UnbakedStandaloneModel<BlockStateModel> {
        private final BlockState state;

        public VerticalWrapper(BlockState state) {
            this.state = state;
        }

        @Override
        @NotNull
        public BlockStateModel bake(@NotNull Baker baker) {
            return VerticalSlabModel.INSTANCE.bake(state, baker);
        }

        @Override
        public void resolve(Resolver resolver) {
            VerticalSlabModel.INSTANCE.resolve(resolver);
        }
    }
}
