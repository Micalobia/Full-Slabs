package dev.micalobia.fullslabs.client.models;

import dev.micalobia.fullslabs.VerticalSlabBlock;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;

public final class VerticalModelRecipe {
    public enum TextureRole {
        SIDE, TOP, BOTTOM
    }

    public record QuadSpec(Direction face, int x1, int y1, int z1, int x2, int y2, int z2, TextureRole role,
                           Direction cullFace, boolean shaded) {
    }

    public record ModelGeometrySpec(List<QuadSpec> quads, boolean fullCube) {
    }

    private VerticalModelRecipe() {
    }

    public static ModelGeometrySpec build(VariantKey key) {
        if (key.type() == VerticalSlabBlock.VerticalType.DOUBLE) return fullCube();

        var alongX = key.axis() == Direction.Axis.X;
        var positive = key.type() == VerticalSlabBlock.VerticalType.POSITIVE;

        int x1 = 0, x2 = 16, z1 = 0, z2 = 16;
        if (alongX) {
            x1 = positive ? 8 : 0;
            x2 = positive ? 16 : 8;
        } else {
            z1 = positive ? 8 : 0;
            z2 = positive ? 16 : 8;
        }

        return cuboid(x1, 0, z1, x2, 16, z2);
    }

    private static ModelGeometrySpec fullCube() {
        return cuboid(0, 0, 0, 16, 16, 16, true);
    }

    private static ModelGeometrySpec cuboid(int x1, int y1, int z1, int x2, int y2, int z2) {
        return cuboid(x1, y1, z1, x2, y2, z2, false);
    }

    private static ModelGeometrySpec cuboid(int x1, int y1, int z1, int x2, int y2, int z2, boolean fullCube) {
        List<QuadSpec> faces = new ArrayList<>(6);
        faces.add(new QuadSpec(Direction.UP, x1, y2, z1, x2, y2, z2, TextureRole.TOP, Direction.UP, true));
        faces.add(new QuadSpec(Direction.DOWN, x1, y1, z1, x2, y1, z2, TextureRole.BOTTOM, Direction.DOWN, true));
        faces.add(new QuadSpec(Direction.NORTH, x1, y1, z1, x2, y2, z1, TextureRole.SIDE, Direction.NORTH, true));
        faces.add(new QuadSpec(Direction.SOUTH, x1, y1, z2, x2, y2, z2, TextureRole.SIDE, Direction.SOUTH, true));
        faces.add(new QuadSpec(Direction.WEST, x1, y1, z1, x1, y2, z2, TextureRole.SIDE, Direction.WEST, true));
        faces.add(new QuadSpec(Direction.EAST, x2, y1, z1, x2, y2, z2, TextureRole.SIDE, Direction.EAST, true));
        return new ModelGeometrySpec(faces, fullCube);
    }
}
