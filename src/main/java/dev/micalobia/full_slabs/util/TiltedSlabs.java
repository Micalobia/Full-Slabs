package dev.micalobia.full_slabs.util;

import net.minecraft.block.Block;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Set;

public class TiltedSlabs {
	private static final Set<Identifier> tiltDoubleSet = new HashSet<>();
	private static final Set<Identifier> tiltSingleSet = new HashSet<>();

	static {
		register("minecraft:smooth_stone_slab");
		register("mo_glass:glass_slab", false, true);
		for(DyeColor color : DyeColor.values()) {
			register(String.format("mo_glass:%s_stained_glass_slab", color.asString()), false, true);
		}
	}

	private static void register(String id) {
		register(new Identifier(id), true, true);
	}

	private static void register(String id, boolean tiltDouble, boolean tiltSingle) {
		register(new Identifier(id), tiltDouble, tiltSingle);
	}

	private static void register(Identifier id) {
		register(id, true, true);
	}

	private static void register(Identifier slab, boolean tiltDouble, boolean tiltSingle) {
		if(tiltDouble) tiltDoubleSet.add(slab);
		if(tiltSingle) tiltSingleSet.add(slab);
	}

	public static boolean isDouble(Identifier id) {
		return tiltDoubleSet.contains(id);
	}

	public static boolean isDouble(Block slab) {
		return isDouble(Helper.fetchId(LinkedSlabs.horizontal(slab)));
	}

	public static boolean isSingle(Identifier id) {
		return tiltSingleSet.contains(id);
	}

	public static boolean isSingle(Block slab) {
		return isSingle(Helper.fetchId(LinkedSlabs.horizontal(slab)));
	}
}
