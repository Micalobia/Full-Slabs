package dev.micalobia.fullslabs.client.models;

import dev.micalobia.fullslabs.FullSlabs;
import dev.micalobia.fullslabs.VerticalSlabBlock;
import dev.micalobia.fullslabs.VerticalSlabBlock.VerticalType;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.client.data.ModelIds;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.ModelElement;
import net.minecraft.client.render.model.json.ModelElementFace;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.AxisRotation;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import org.joml.Vector3f;

import java.util.EnumMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

public class VerticalSlabModel implements BlockStateModel.UnbakedGrouped {
    private final SlabBlock parent;
    private final Axis axis;
    private final VerticalType type;

    public VerticalSlabModel(BlockState state) {
        if (!(state.getBlock() instanceof VerticalSlabBlock slab))
            throw new IllegalArgumentException("Needs to be a blockstate of a vertical slab!");
        this.parent = slab.parent;
        this.axis = state.get(VerticalSlabBlock.AXIS);
        this.type = state.get(VerticalSlabBlock.TYPE);
    }

    @Override
    public BlockStateModel bake(BlockState state, Baker baker) {
        var x1 = 0f;
        var y1 = 0f;
        var z1 = 0f;
        var x2 = 16f;
        var y2 = 16f;
        var z2 = 16f;
        if (type != VerticalType.DOUBLE) {
            if (axis == Axis.X) {
                x1 = type == VerticalType.NEGATIVE ? 0f : 8f;
                x2 = type == VerticalType.NEGATIVE ? 8f : 16f;
            } else if (axis == Axis.Z) {
                z1 = type == VerticalType.NEGATIVE ? 0f : 8f;
                z2 = type == VerticalType.NEGATIVE ? 8f : 16f;
            }
        }
        var uvUpDown = new ModelElementFace.UV(x1, z1, x2, z2);
        var uvNorthSouth = new ModelElementFace.UV(x1, y1, x2, y2);
        var uvEastWest = new ModelElementFace.UV(z1, y1, z2, y2);
        var faces = new EnumMap<Direction, ModelElementFace>(Direction.class);
        faces.put(Direction.UP, new ModelElementFace(null, -1, "#v_top", uvUpDown, AxisRotation.R0));
        faces.put(Direction.DOWN, new ModelElementFace(null, -1, "#v_bottom", uvUpDown, AxisRotation.R180));
        faces.put(Direction.NORTH, new ModelElementFace(null, -1, "#v_side", uvNorthSouth, AxisRotation.R0));
        faces.put(Direction.EAST, new ModelElementFace(null, -1, "#v_side", uvEastWest, AxisRotation.R0));
        faces.put(Direction.SOUTH, new ModelElementFace(null, -1, "#v_side", uvNorthSouth, AxisRotation.R0));
        faces.put(Direction.WEST, new ModelElementFace(null, -1, "#v_side", uvEastWest, AxisRotation.R0));
        var from = new Vector3f(x1, y1, z1);
        var to = new Vector3f(x2, y2, z2);
        var element = new ModelElement(from, to, faces);

        var parentModelId = ModelIds.getBlockModelId(this.parent);
        var parent = baker.getModel(parentModelId);
        var parentTextures = parent.getTextures();
        var parentGeometry = parent.bakeGeometry(parentTextures, baker, ModelRotation.X0_Y0);
        SimpleModel simple = () -> "fullslabs:vertical_slab";
        var particle = parent.getParticleTexture(parentTextures, baker);
        Function<Direction, Sprite> pick = dir -> parentGeometry.getQuads(dir).stream().map(BakedQuad::sprite).findFirst().orElseGet(() -> parentGeometry.getQuads(null).stream().filter(q -> q.face() == dir).map(BakedQuad::sprite).findFirst().orElse(particle));
        var side = Stream.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST).map(pick).filter(Objects::nonNull).findFirst().orElse(particle);
        var top = pick.apply(Direction.UP);
        var bottom = pick.apply(Direction.DOWN);

        final Identifier ATLAS = SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
        final Identifier V_SIDE = FullSlabs.id("internal/v_side");
        final Identifier V_TOP = FullSlabs.id("internal/v_top");
        final Identifier V_BOTTOM = FullSlabs.id("internal/v_bottom");
        final Identifier V_PARTICLE = FullSlabs.id("internal/v_particle");

        var table = new ModelTextures.Textures.Builder().addSprite("v_side", new SpriteIdentifier(ATLAS, V_SIDE)).addSprite("v_top", new SpriteIdentifier(ATLAS, V_TOP)).addSprite("v_bottom", new SpriteIdentifier(ATLAS, V_BOTTOM)).addSprite("particle", new SpriteIdentifier(ATLAS, V_PARTICLE)).build();

        var mapped = new ModelTextures.Builder().addLast(table).build(simple);

        var fallback = baker.getSpriteGetter();
        var spriteGetter = new ErrorCollectingSpriteGetter() {
            @Override
            public Sprite get(SpriteIdentifier id, SimpleModel model) {
                var tex = id.getTextureId();
                if (tex.equals(V_SIDE)) return side;
                if (tex.equals(V_TOP)) return top;
                if (tex.equals(V_BOTTOM)) return bottom;
                if (tex.equals(V_PARTICLE)) return particle;
                return fallback.get(id, model);
            }

            @Override
            public Sprite getMissing(String name, SimpleModel model) {
                return fallback.getMissing(name, model);
            }
        };

        var quads = UnbakedGeometry.bakeGeometry(List.of(element), mapped, spriteGetter, ModelRotation.X0_Y0, simple);
        boolean useAO = BakedSimpleModel.getAmbientOcclusion(parent);
        var part = new GeometryBakedModel(quads, useAO, particle);
        return new SimpleBlockStateModel(part);
    }

    @Override
    public Object getEqualityGroup(BlockState state) {
        // Not sure what this is supposed to return
        return null;
    }

    @Override
    public void resolve(Resolver resolver) {

    }
}
