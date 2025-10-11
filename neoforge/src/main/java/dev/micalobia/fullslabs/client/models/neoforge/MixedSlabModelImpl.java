package dev.micalobia.fullslabs.client.models.neoforge;

import dev.micalobia.fullslabs.FullSlabs;
import dev.micalobia.fullslabs.neoforge.FullSlabsNeoForge;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.annotation.MethodsReturnNonnullByDefault;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

public class MixedSlabModelImpl {
    public static BlockStateModel create(BlockState state, Baker baker) {
        return Model.INSTANCE;
    }

    @ParametersAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    public static final class Model implements BlockStateModel {
        public static Model INSTANCE = new Model();

        @Override
        public void addParts(Random random, List<BlockModelPart> parts) {
            // no-op
        }

        @Override
        public void collectParts(BlockRenderView world, BlockPos pos, BlockState state, Random random, List<BlockModelPart> parts) {
            var data = world.getModelData(pos);
            var ctx = data.get(FullSlabsNeoForge.MIXED_CONTEXT_MODEL_PROPERTY);
            if (ctx == null) {
                FullSlabs.LOGGER.warn("Mixed slab didn't send model data!");
                return;
            }
            var towards = ctx.towardsState();
            var away = ctx.awayState();
            var client = MinecraftClient.getInstance();
            var manager = client.getBlockRenderManager();
            var towardsModel = manager.getModel(towards);
            var awayModel = manager.getModel(away);
            towardsModel.collectParts(world, pos, state, random, parts);
            awayModel.collectParts(world, pos, state, random, parts);
        }

        @SuppressWarnings("deprecation")
        @Override
        public Sprite particleSprite() {
            return MinecraftClient.getInstance().getBakedModelManager().getMissingModel().particleSprite();
        }

        @Override
        public Sprite particleIcon(BlockRenderView level, BlockPos pos, BlockState state) {
            return MinecraftClient.getInstance().getBakedModelManager().getMissingModel().particleIcon(level, pos, state);
        }
    }
}
