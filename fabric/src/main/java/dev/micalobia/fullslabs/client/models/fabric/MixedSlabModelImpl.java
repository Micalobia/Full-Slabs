package dev.micalobia.fullslabs.client.models.fabric;

import dev.micalobia.fullslabs.FullSlabs;
import dev.micalobia.fullslabs.block.entity.MixedSlabBlockEntity.ModelContext;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBlockStateModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

@SuppressWarnings("unused")
public class MixedSlabModelImpl {
    public static BlockStateModel create(BlockState state, ModelBaker baker) {
        return Model.INSTANCE;
    }

    public static final class Model implements BlockStateModel, FabricBlockStateModel {
        public static Model INSTANCE = new Model();

        @Override
        public void emitQuads(QuadEmitter emitter, BlockAndTintGetter view, BlockPos pos, BlockState state, RandomSource random, Predicate<@Nullable Direction> cullTest) {
            var data = view.getBlockEntityRenderData(pos);
            if (!(data instanceof ModelContext ctx)) {
                FullSlabs.LOGGER.warn("Mixed slab didn't send model data!");
                return;
            }
            var towards = ctx.towardsState();
            var away = ctx.awayState();
            var client = Minecraft.getInstance();
            var manager = client.getBlockRenderer();
            var towardsModel = manager.getBlockModel(towards);
            var awayModel = manager.getBlockModel(away);
            towardsModel.emitQuads(emitter, view, pos, towards, random, cullTest);
            awayModel.emitQuads(emitter, view, pos, away, random, cullTest);
        }

        @Override
        public void collectParts(RandomSource random, List<BlockModelPart> parts) {
        }

        @Override
        @NotNull
        public TextureAtlasSprite particleIcon() {
            // This isn't actually called because of mixin stuff
            return Minecraft.getInstance().getModelManager().getMissingBlockStateModel().particleIcon();
        }
    }
}
