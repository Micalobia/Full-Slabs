package dev.micalobia.full_slabs.mixin.client.render.model;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import dev.micalobia.full_slabs.util.Helper;
import dev.micalobia.full_slabs.client.render.model.SlabModelInfo;
import net.minecraft.block.Block;
import net.minecraft.block.SlabBlock;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelVariant;
import net.minecraft.client.render.model.json.ModelVariantMap;
import net.minecraft.client.render.model.json.WeightedUnbakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.state.StateManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Map;

@Mixin(ModelLoader.class)
public class ModelLoaderMixin {
	@Inject(method = "loadModel", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
	private void collectSprites(Identifier id, CallbackInfo ci, Identifier identifier2, StateManager stateManager, List list, ImmutableList immutableList, Map map, Map map2, Identifier identifier3, UnbakedModel unbakedModel, ModelLoader.ModelDefinition modelDefinition, Pair pair, List<Pair<String, ModelVariantMap>> list3) {
		Block block = Helper.fetchBlock((ModelIdentifier) id);
		if(!(block instanceof SlabBlock)) return;
		for(Pair<String, ModelVariantMap> obj : list3) {
			ModelVariantMap variantMap = obj.getSecond();
			Map<String, WeightedUnbakedModel> variants = variantMap.getVariantMap();
			for(String key : variants.keySet()) {
				List<ModelVariant> vars = variants.get(key).getVariants();
				for(ModelVariant variant : vars) {
					SlabModelInfo.put(block, variant);
				}
			}
		}
	}
}
