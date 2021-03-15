package dev.micalobia.full_slabs.client.render.model;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.json.JsonUnbakedModel;

import java.util.HashMap;
import java.util.Map;

public class JsonUnbakedModelIndex {
	private static final Map<BlockState, JsonUnbakedModel> models = new HashMap<>();

	public static void put(BlockState state, JsonUnbakedModel model) {
		models.put(state, model);
	}

	public static JsonUnbakedModel get(BlockState state, JsonUnbakedModel model) {
		return models.get(state);
	}

}
