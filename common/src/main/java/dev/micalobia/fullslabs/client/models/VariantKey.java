package dev.micalobia.fullslabs.client.models;


import dev.micalobia.fullslabs.VerticalSlabBlock;
import net.minecraft.util.math.Direction;

public record VariantKey(Direction.Axis axis, VerticalSlabBlock.VerticalType type) {
}
