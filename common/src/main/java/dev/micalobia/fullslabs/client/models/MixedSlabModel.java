package dev.micalobia.fullslabs.client.models;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.BlockStateModel;

public final class MixedSlabModel implements BlockStateModel.UnbakedGrouped {
    public static final MixedSlabModel INSTANCE = new MixedSlabModel();

    @Override
    public BlockStateModel bake(BlockState state, Baker baker) {
        return create(state, baker);
    }

    @Override
    public Object getEqualityGroup(BlockState state) {
        return null;
    }

    @Override
    public void resolve(Resolver resolver) {
    }

    @ExpectPlatform
    public static BlockStateModel create(BlockState state, Baker baker) {
        throw new AssertionError();
    }
}
