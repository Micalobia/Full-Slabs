package dev.micalobia.full_slabs.config;


import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import dev.micalobia.full_slabs.util.Utility;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Config(name = "full_slabs")
public class ModConfig implements ConfigData {
	Set<String> tiltableSlabs = ImmutableSet.of("minecraft:smooth_stone_slab");
	Map<String, SlabExtraConfig> slabExtras = ImmutableMap.<String, SlabExtraConfig>builder()
			.put("minecraft:wall_torch", new SlabExtraConfig(null, null, "facing=south", "facing=north", "facing=west", "facing=east"))
			.put("minecraft:torch", new SlabExtraConfig("", null, null, null, null, null))
			.put("minecraft:soul_wall_torch", new SlabExtraConfig(null, null, "facing=south", "facing=north", "facing=west", "facing=east"))
			.put("minecraft:soul_torch", new SlabExtraConfig("", null, null, null, null, null))
			.put("minecraft:lantern", new SlabExtraConfig("hanging=false", "hanging=true", null, null, null, null))
			.put("minecraft:soul_lantern", new SlabExtraConfig("hanging=false", "hanging=true", null, null, null, null))
			.put("minecraft:snow", new SlabExtraConfig("layers=1", null, null, null, null, null))
			.put("minecraft:white_carpet", new SlabExtraConfig("", null, null, null, null, null))
			.put("minecraft:orange_carpet", new SlabExtraConfig("", null, null, null, null, null))
			.put("minecraft:magenta_carpet", new SlabExtraConfig("", null, null, null, null, null))
			.put("minecraft:light_blue_carpet", new SlabExtraConfig("", null, null, null, null, null))
			.put("minecraft:yellow_carpet", new SlabExtraConfig("", null, null, null, null, null))
			.put("minecraft:lime_carpet", new SlabExtraConfig("", null, null, null, null, null))
			.put("minecraft:pink_carpet", new SlabExtraConfig("", null, null, null, null, null))
			.put("minecraft:gray_carpet", new SlabExtraConfig("", null, null, null, null, null))
			.put("minecraft:light_gray_carpet", new SlabExtraConfig("", null, null, null, null, null))
			.put("minecraft:cyan_carpet", new SlabExtraConfig("", null, null, null, null, null))
			.put("minecraft:purple_carpet", new SlabExtraConfig("", null, null, null, null, null))
			.put("minecraft:blue_carpet", new SlabExtraConfig("", null, null, null, null, null))
			.put("minecraft:brown_carpet", new SlabExtraConfig("", null, null, null, null, null))
			.put("minecraft:green_carpet", new SlabExtraConfig("", null, null, null, null, null))
			.put("minecraft:red_carpet", new SlabExtraConfig("", null, null, null, null, null))
			.put("minecraft:black_carpet", new SlabExtraConfig("", null, null, null, null, null))
			.put("minecraft:moss_carpet", new SlabExtraConfig("", null, null, null, null, null))
			.build();

	public ModConfig() {
	}

	public Set<Identifier> getTiltableSlabs() {
		return tiltableSlabs.stream().map(Identifier::new).collect(Collectors.toSet());
	}

	public Map<Identifier, SlabExtra> getSlabExtras() {
		HashMap<Identifier, SlabExtra> ret = new HashMap<>();
		slabExtras.forEach(((s, e) -> ret.put(new Identifier(s), e.convert(s))));
		return ret;
	}

	static class SlabExtraConfig {
		public @Nullable String bottom;
		public @Nullable String top;
		public @Nullable String north;
		public @Nullable String south;
		public @Nullable String east;
		public @Nullable String west;

		public SlabExtraConfig(
				@Nullable String bottom, @Nullable String top,
				@Nullable String north, @Nullable String south,
				@Nullable String east, @Nullable String west
		) {
			this.bottom = bottom;
			this.top = top;
			this.north = north;
			this.south = south;
			this.east = east;
			this.west = west;
		}

		public SlabExtra convert(String block) {
			return new SlabExtra(Utility.getBlock(new Identifier(block)), bottom, top, north, south, east, west);
		}
	}
}
