package dev.micalobia.fullslabs.client.models.neoforge;

import dev.micalobia.fullslabs.FullSlabs;
import dev.micalobia.fullslabs.neoforge.FullSlabsNeoForge;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

public class MixedSlabModelImpl {
    public static BlockStateModel create(BlockState state, ModelBaker baker) {
        return Model.INSTANCE;
    }

    @ParametersAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    public static final class Model implements BlockStateModel {
        public static final Model INSTANCE = new Model();

        @Override
        public void collectParts(RandomSource random, List<BlockModelPart> parts) {
            // no-op
        }

        @Override
        public void collectParts(BlockAndTintGetter world, BlockPos pos, BlockState state, RandomSource random, List<BlockModelPart> parts) {
            var data = world.getModelData(pos);
            var ctx = data.get(FullSlabsNeoForge.MIXED_CONTEXT_MODEL_PROPERTY);
            if (ctx == null) {
                FullSlabs.LOGGER.warn("Mixed slab didn't send model data!");
                return;
            }
            var towards = ctx.towardsState();
            var away = ctx.awayState();
            var client = Minecraft.getInstance();
            var manager = client.getBlockRenderer();
            var towardsModel = manager.getBlockModel(towards);
            var awayModel = manager.getBlockModel(away);
            towardsModel.collectParts(world, pos, state, random, parts);
            awayModel.collectParts(world, pos, state, random, parts);
        }

        @SuppressWarnings("deprecation")
        @Override
        public TextureAtlasSprite particleIcon() {
            return Minecraft.getInstance().getModelManager().getMissingBlockStateModel().particleIcon();
        }

        @Override
        public TextureAtlasSprite particleIcon(BlockAndTintGetter level, BlockPos pos, BlockState state) {
            return Minecraft.getInstance().getModelManager().getMissingBlockStateModel().particleIcon(level, pos, state);
        }
    }
}
