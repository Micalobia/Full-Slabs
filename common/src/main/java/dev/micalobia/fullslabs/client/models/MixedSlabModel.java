package dev.micalobia.fullslabs.client.models;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.world.level.block.state.BlockState;

@MethodsReturnNonnullByDefault
public final class MixedSlabModel implements BlockStateModel.UnbakedRoot {
    public static final MixedSlabModel INSTANCE = new MixedSlabModel();

    @Override
    public BlockStateModel bake(BlockState state, ModelBaker baker) {
        return create(state, baker);
    }

    @Override
    public Object visualEqualityGroup(BlockState state) {
        return this;
    }

    @Override
    public void resolveDependencies(Resolver resolver) {
    }

    @ExpectPlatform
    public static BlockStateModel create(BlockState state, ModelBaker baker) {
        throw new AssertionError();
    }
}
