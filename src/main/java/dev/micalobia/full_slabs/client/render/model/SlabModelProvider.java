package dev.micalobia.full_slabs.client.render.model;

import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelProviderException;
import net.fabricmc.fabric.api.client.model.ModelResourceProvider;
import net.fabricmc.fabric.api.client.model.ModelVariantProvider;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderingRegistry.ModelProvider;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class SlabModelProvider implements ModelVariantProvider {

	@Override
	public @Nullable UnbakedModel loadModelVariant(ModelIdentifier modelIdentifier, ModelProviderContext modelProviderContext) throws ModelProviderException {
		if (!"full_slabs".equals(modelIdentifier.getNamespace())) return null;
		String path = modelIdentifier.getPath();
		if ("full_slab_block".equals(path)) return null; //TODO: Make FullSlabBlockModel
		return new VerticalSlabModel(modelIdentifier);
	}
}
