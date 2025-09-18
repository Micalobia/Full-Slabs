package dev.micalobia.fullslabs.mixin.client;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.render.model.ModelBaker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ModelBaker.class)
public interface ModelBakerAccessor {
    @Accessor("blockModels")
    Map<BlockState, BlockStateModel.UnbakedGrouped> fullslabs$getBlockModels();
}
