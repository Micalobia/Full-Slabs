package dev.micalobia.full_slabs.util;

import dev.micalobia.full_slabs.block.VerticalSlabBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SlabBlock;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class LinkedSlabs {
	private static final Map<SlabBlock, VerticalSlabBlock> vertical;
	private static final Map<VerticalSlabBlock, SlabBlock> horizontal;

	static {
		vertical = new HashMap<>();
		horizontal = new HashMap<>();
	}

	public static void link(SlabBlock slab, VerticalSlabBlock verticalSlab) {
		vertical.put(slab, verticalSlab);
		horizontal.put(verticalSlab, slab);
	}

	public static void link(Block slab, Block verticalSlab) {
		link((SlabBlock) slab, (VerticalSlabBlock) verticalSlab);
	}

	public static boolean contains(Block slab) {
		return vertical.containsKey(slab) || horizontal.containsKey(slab);
	}

	public static boolean contains(Block slab0, Block slab1) {
		return contains(slab0) && contains(slab1);
	}

	public static boolean contains(Block... slabs) {
		for(Block slab : slabs) {
			if(!contains(slab)) return false;
		}
		return true;
	}

	@Nullable
	public static SlabBlock horizontal(Block slab) {
		if(!contains(slab)) return null;
		if(slab instanceof SlabBlock) return (SlabBlock) slab;
		if(slab instanceof VerticalSlabBlock) return horizontal.get(slab);
		throw new RuntimeException("The block you tried to get is linked, but not a slab!");
	}

	@Nullable
	public static VerticalSlabBlock vertical(Block slab) {
		if(!contains(slab)) return null;
		if(slab instanceof SlabBlock) return vertical.get(slab);
		if(slab instanceof VerticalSlabBlock) return (VerticalSlabBlock) slab;
		throw new RuntimeException("The block you tried to get is linked, but not a slab!");
	}

	public static boolean linked(Block slab, Block verticalSlab) {
		if(!contains(slab, verticalSlab)) return false;
		return slab instanceof SlabBlock &&
				verticalSlab instanceof VerticalSlabBlock &&
				horizontal(slab) == horizontal(verticalSlab);
	}
}
