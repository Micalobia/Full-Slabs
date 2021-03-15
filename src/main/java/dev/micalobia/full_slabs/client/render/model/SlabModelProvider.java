package dev.micalobia.full_slabs.client.render.model;

import dev.micalobia.full_slabs.block.VerticalSlabBlock;
import dev.micalobia.full_slabs.util.Helper;
import dev.micalobia.full_slabs.util.LinkedSlabs;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelProviderException;
import net.fabricmc.fabric.api.client.model.ModelResourceProvider;
import net.fabricmc.fabric.api.client.model.ModelVariantProvider;
import net.minecraft.block.SlabBlock;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class SlabModelProvider implements ModelVariantProvider, ModelResourceProvider {
	@Override
	public @Nullable UnbakedModel loadModelVariant(ModelIdentifier modelIdentifier, ModelProviderContext modelProviderContext) throws ModelProviderException {
		if(!"full_slabs".equals(modelIdentifier.getNamespace())) return null;
		String path = modelIdentifier.getPath();
		if("full_slab_block".equals(path)) return null;
		// Vertical Slab block area
		VerticalSlabBlock vertical = (VerticalSlabBlock) Helper.fetchBlock(new Identifier(modelIdentifier.getNamespace(), modelIdentifier.getPath()));
		SlabBlock base = LinkedSlabs.horizontal(vertical);
		Identifier id = Helper.fetchId(base);
		JsonUnbakedModel json = (JsonUnbakedModel) modelProviderContext.loadModel(new Identifier(id.getNamespace(), "block/" + id.getPath()));
		return JsonUnbakedModel.deserialize(VerticalSlabBlock.generateJson(
				vertical.fromModelIdentifier(modelIdentifier),
				base,
				json.resolveSprite("bottom"),
				json.resolveSprite("top"),
				json.resolveSprite("side")
		));
	}

	@Override
	public @Nullable UnbakedModel loadModelResource(Identifier identifier, ModelProviderContext modelProviderContext) throws ModelProviderException {
		if("full_slabs:block/full_slab_block".equals(identifier.toString()))
			return new FullSlabModel();
		return null;
	}
}
