package dev.micalobia.full_slabs.block.enums;

import net.minecraft.block.enums.SlabType;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import org.jetbrains.annotations.Nullable;

public enum SlabState implements StringIdentifiable {
	POSITIVE("positive"),
	NEGATIVE("negative"),
	DOUBLE("double");

	private final String name;

	private SlabState(String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}

	public String asString() {
		return name;
	}

	@Nullable
	public Direction direction(Axis axis) {
		if (this == DOUBLE) return null;
		switch(axis) {
			case X: return this == POSITIVE ? Direction.EAST : Direction.WEST;
			case Z: return this == POSITIVE ? Direction.SOUTH : Direction.NORTH;
			default: return this == POSITIVE ? Direction.UP : Direction.DOWN;
		}
	}

	public SlabType slabType() {
		switch(this) {
			case POSITIVE: return SlabType.TOP;
			case NEGATIVE: return SlabType.BOTTOM;
			default: return SlabType.DOUBLE;
		}
	}

	public static SlabState fromSlabType(SlabType type) {
		switch(type) {
			case TOP: return POSITIVE;
			case BOTTOM: return NEGATIVE;
			default: return DOUBLE;
		}
	}
}
