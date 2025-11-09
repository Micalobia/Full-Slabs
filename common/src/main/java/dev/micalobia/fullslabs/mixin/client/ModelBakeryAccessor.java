package dev.micalobia.fullslabs.mixin.client;

import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ModelBakery.class)
public interface ModelBakeryAccessor {
    @Accessor("unbakedBlockStateModels")
    Map<BlockState, BlockStateModel.UnbakedRoot> fullslabs$getUnbakedBlockStateModels();
}
