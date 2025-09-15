package dev.micalobia.fullslabs.client.models;

import dev.micalobia.fullslabs.FullSlabs;
import dev.micalobia.fullslabs.VerticalSlabBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import java.util.Arrays;
import java.util.Objects;

public final class VerticalModels {
    private VerticalModels() {
    }

    public static Identifier makeModelId(Identifier parentBlockId, Direction.Axis axis, VerticalSlabBlock.VerticalType type) {
        var path = String.format("block/%s/%s_%s", FullSlabs.verticalPath(parentBlockId), axis.asString().toLowerCase(), type.asString().toLowerCase());
        return FullSlabs.id(path);
    }

    public static Identifier makeModelId(BlockState state) {
        if (!(state.getBlock() instanceof VerticalSlabBlock)) throw new RuntimeException("Not a vertical slab!");
        return FullSlabs.id(String.format("block/%s/%s_%s", Registries.BLOCK.getId(state.getBlock()).getPath(), state.get(VerticalSlabBlock.AXIS).asString(), state.get(VerticalSlabBlock.TYPE).asString()));
    }

    public static ParsedVerticalId parseModelId(Identifier id) {
        var path = id.getPath();
        var parts = path.split("/");
        if (parts.length < 5 || !Objects.equals(parts[0], "block") || !Objects.equals(parts[1], "vertical"))
            return null;
        var parentNs = parts[2];
        var parentPath = String.join("/", Arrays.copyOfRange(parts, 3, parts.length - 2));
        var variant = parts[parts.length - 1];

        var vt = variant.split("_");
        if (vt.length != 2) return null;

        var axis = "z".equals(vt[0]) ? Direction.Axis.Z : Direction.Axis.X;
        var type = switch (vt[1]) {
            case "negative" -> VerticalSlabBlock.VerticalType.NEGATIVE;
            case "positive" -> VerticalSlabBlock.VerticalType.POSITIVE;
            case "double" -> VerticalSlabBlock.VerticalType.DOUBLE;
            default -> null;
        };
        if (type == null) return null;
        var parentId = Identifier.of(parentNs, parentPath);
        var parent = Registries.BLOCK.get(parentId);
        if (!(parent instanceof SlabBlock slab)) return null;


        return new ParsedVerticalId(slab, new VariantKey(axis, type));
    }

    public static VerticalModelRecipe.ModelGeometrySpec geometry(VariantKey key) {
        return VerticalModelRecipe.build(key);
    }

    public record ParsedVerticalId(SlabBlock parent, VariantKey key) {
    }
}
