package dev.micalobia.full_slabs.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;
import virtuoel.statement.api.StateRefresher;

import java.util.Optional;

public class Utility {
	public static HitResult crosshair(PlayerEntity player) {
		return player.raycast(player.isCreative() ? 5.0d : 4.5d, 0f, false);
	}

	public static Identifier getBlockId(Block block) {
		return Registry.BLOCK.getId(block);
	}

	public static Block getBlock(Identifier id) {
		return Registry.BLOCK.get(id);
	}

	public static BlockState getStateFromString(Block block, @Nullable String string) {
		if(string == null) return null;
		StateManager<Block, BlockState> manager = block.getStateManager();
		String[] properties = string.split(",");
		BlockState state = block.getDefaultState();
		for(String str : properties) {
			String[] pair = str.split("=");
			String name = pair[0];
			if(pair.length != 2) continue;
			String value = pair[1];
			Property<?> property = manager.getProperty(name);
			if(property == null) continue;
			state = with(state, property, value);
		}
		return state;
	}

	private static <T extends Comparable<T>> BlockState with(BlockState state, Property<T> property, String value) {
		Optional<T> ret = property.parse(value);
		if(ret.isPresent()) return state.with(property, ret.get());
		return state;
	}

	public static <T extends Comparable<T>> void injectBlockProperty(Class<? extends Block> cls, Property<T> property, T defaultValue) {
		for(Block block : Registry.BLOCK) {
			if(cls.isAssignableFrom(block.getClass())) {
				injectBlockProperty(block, property, defaultValue);
			}
		}
	}

	public static <T extends Comparable<T>> void injectBlockProperty(Block block, Property<T> property, T defaultValue) {
		StateRefresher.INSTANCE.addBlockProperty(block, property, defaultValue);
	}
}
