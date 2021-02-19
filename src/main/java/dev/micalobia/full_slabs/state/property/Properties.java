package dev.micalobia.full_slabs.state.property;

import dev.micalobia.full_slabs.block.enums.SlabState;
import net.minecraft.state.property.EnumProperty;

public class Properties {
	public static final EnumProperty<SlabState> SLAB_STATE;

	static {
		SLAB_STATE = EnumProperty.of("state", SlabState.class, SlabState.values());
	}
}
