package dev.micalobia.fullslabs.client.models.fabric;

import dev.micalobia.fullslabs.FullSlabs;
import dev.micalobia.fullslabs.block.entity.MixedSlabBlockEntity.ModelContext;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBlockStateModel;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class MixedSlabModelImpl {
    public static BlockStateModel create(BlockState state, Baker baker) {
        return Model.INSTANCE;
    }

    public static final class Model implements BlockStateModel, FabricBlockStateModel {
        public static Model INSTANCE = new Model();

        @Override
        public void emitQuads(QuadEmitter emitter, BlockRenderView view, BlockPos pos, BlockState state, Random random, Predicate<@Nullable Direction> cullTest) {
            var data = view.getBlockEntityRenderData(pos);
            if (!(data instanceof ModelContext ctx)) {
                FullSlabs.LOGGER.warn("Mixed slab didn't send model data!");
                return;
            }
            var towards = ctx.towardsState();
            var away = ctx.awayState();
            var client = MinecraftClient.getInstance();
            var manager = client.getBlockRenderManager();
            var towardsModel = manager.getModel(towards);
            var awayModel = manager.getModel(away);
            towardsModel.emitQuads(emitter, view, pos, towards, random, cullTest);
            awayModel.emitQuads(emitter, view, pos, away, random, cullTest);
        }

        @Override
        public void addParts(Random random, List<BlockModelPart> parts) {
        }

        @Override
        public Sprite particleSprite() {
            // This isn't actually called because of mixin stuff
            return MinecraftClient.getInstance().getBakedModelManager().getMissingModel().particleSprite();
        }
    }
}
