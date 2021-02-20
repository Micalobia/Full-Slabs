package dev.micalobia.full_slabs.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

	public static Item fetchItem(Identifier id) {
		return Registry.ITEM.get(id);
	}

	public static Identifier fetchId(Item item) {
		return Registry.ITEM.getId(item);
	}

	public static Block fetchBlock(Item item) {
		return fetchBlock(fetchId(item));
	}

	public static boolean isVerticalId(Identifier id) {
		if (!"full_slabs".equals(id.getNamespace())) return false;
		return id.getPath().endsWith("_vertical");
	}
}
