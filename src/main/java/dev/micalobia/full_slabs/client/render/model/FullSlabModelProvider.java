package dev.micalobia.full_slabs.client.render.model;

import dev.micalobia.full_slabs.FullSlabsMod;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelProviderException;
import net.fabricmc.fabric.api.client.model.ModelResourceProvider;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class FullSlabModelProvider implements ModelResourceProvider {
	private static final Identifier FULL_SLAB_BLOCK_ID = FullSlabsMod.id("block/full_slab_block");

	@Override
	public @Nullable UnbakedModel loadModelResource(Identifier resourceId, ModelProviderContext context) throws ModelProviderException {
		if(FULL_SLAB_BLOCK_ID.equals(resourceId)) return new FullSlabModel();
		return null;
	}
}
