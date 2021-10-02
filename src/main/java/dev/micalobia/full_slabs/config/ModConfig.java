package dev.micalobia.full_slabs.config;


import com.google.common.collect.ImmutableSet;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import net.minecraft.util.Identifier;

import java.util.Set;
import java.util.stream.Collectors;

@Config(name = "full_slabs")
public class ModConfig implements ConfigData {
	Set<String> tiltableSlabs = ImmutableSet.of("minecraft:smooth_stone_slab");

	public Set<Identifier> getTiltableSlabs() {
		return tiltableSlabs.stream().map(Identifier::new).collect(Collectors.toSet());
	}
}
