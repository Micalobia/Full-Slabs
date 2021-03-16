package dev.micalobia.full_slabs.client.render.model;

import net.minecraft.block.Block;
import net.minecraft.block.enums.SlabType;
import net.minecraft.client.render.model.json.ModelVariant;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class SlabModelInfo {
	private static final Map<Block, Identifier> doubleModels = new HashMap<>();
	private static final Map<Block, Identifier> topModels = new HashMap<>();
	private static final Map<Block, Identifier> bottomModels = new HashMap<>();

	public static Identifier get(Block slab, SlabType type) {
		switch(type) {
			case BOTTOM:
				return bottomModels.get(slab);
			case TOP:
				return topModels.get(slab);
			case DOUBLE:
				return doubleModels.get(slab);
		}
		throw new IllegalArgumentException("type isn't valid; report to FullSlabs");
	}

	public static void put(Block block, ModelVariant variant) {
		Identifier ret = variant.getLocation();
		String path = ret.getPath();
		SlabType type = SlabType.DOUBLE;
		if(path.endsWith("_slab")) type = SlabType.BOTTOM;
		else if(path.endsWith("_slab_top")) type = SlabType.TOP;
		switch(type) {
			case DOUBLE:
				doubleModels.put(block, variant.getLocation());
				break;
			case TOP:
				topModels.put(block, variant.getLocation());
				break;
			case BOTTOM:
				bottomModels.put(block, variant.getLocation());
				break;
		}
	}
}
