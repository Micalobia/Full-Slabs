package dev.micalobia.full_slabs.client.render.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelProviderException;
import net.fabricmc.fabric.api.client.model.ModelResourceProvider;
import net.fabricmc.fabric.api.client.model.ModelVariantProvider;
import net.minecraft.client.render.model.UnbakedModel;
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
		return new VerticalSlabModel(modelIdentifier);
	}

	@Override
	public @Nullable UnbakedModel loadModelResource(Identifier identifier, ModelProviderContext modelProviderContext) throws ModelProviderException {
		if("full_slabs:block/full_slab_block".equals(identifier.toString()))
			return new FullSlabModel();
		return null;
	}
}
