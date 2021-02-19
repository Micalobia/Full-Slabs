package dev.micalobia.full_slabs.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Helper {
	public static Block fetchBlock(Identifier id) {
		return Registry.BLOCK.get(id);
	}

	public static Identifier fetchId(Block block) {
		return Registry.BLOCK.getId(block);
	}

	public static BlockState fetchDefaultState(Identifier id) {
		return fetchBlock(id).getDefaultState();
	}
}
