package dev.micalobia.full_slabs.util;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Set;

public class TiltedSlabs {
	private static final Set<Identifier> tiltedSlabs = new HashSet<>();

	static {
		register(Blocks.SMOOTH_STONE_SLAB);
	}

	public static void register(Block tiltedSlab) {
		tiltedSlabs.add(Helper.fetchId(tiltedSlab));
	}

	public static void register(Identifier tiltedSlab) {
		tiltedSlabs.add(tiltedSlab);
	}

	public static boolean contains(Block slab) {
		return tiltedSlabs.contains(Helper.fetchId(slab));
	}

	public static boolean contains(Identifier slab) {
		return tiltedSlabs.contains(slab);
	}
}
