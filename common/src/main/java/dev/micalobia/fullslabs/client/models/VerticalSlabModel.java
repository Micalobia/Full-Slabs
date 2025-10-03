package dev.micalobia.fullslabs.client.models;

import com.google.common.collect.ImmutableList;
import dev.micalobia.fullslabs.FullSlabs;
import dev.micalobia.fullslabs.block.VerticalSlabBlock;
import dev.micalobia.fullslabs.block.VerticalSlabBlock.VerticalType;
import dev.micalobia.fullslabs.config.Config;
import dev.micalobia.fullslabs.mixin.client.BakerImplOuterAccessor;
import dev.micalobia.fullslabs.mixin.client.ModelBakerAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.client.render.model.*;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class VerticalSlabModel implements BlockStateModel.UnbakedGrouped {
    @SuppressWarnings("deprecation")
    private static final Identifier ATLAS = SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
    public static final VerticalSlabModel INSTANCE = new VerticalSlabModel();

    public static List<Identifier> TEMPLATES = templates();

    private VerticalSlabModel() {
    }

    private static VerticalSlabBlock verifyVertical(Block block) {
        if (!(block instanceof VerticalSlabBlock slab)) throw new IllegalArgumentException();
        return slab;
    }

    public static Identifier templateId(BlockState state) {
        var slab = verifyVertical(state.getBlock());
        var facing = state.get(VerticalSlabBlock.DIRECTION);
        var type = state.get(VerticalSlabBlock.TYPE);
        var tilted = Config.isTilted(slab.parent);
        if (tilted)
            return FullSlabs.id("block/vertical/tilted/%s_%s".formatted(facing.asString(), type.asString()));
        if (type == VerticalType.FULL) return FullSlabs.id("block/vertical/normal/full");
        return FullSlabs.id("block/vertical/normal/%s".formatted((type == VerticalType.AWAY ? facing.getOpposite() : facing).asString()));
    }

    public static BlockState parentState(BlockState state) {
        var slab = verifyVertical(state.getBlock());
        var type = state.get(VerticalSlabBlock.TYPE);
        var parentState = slab.parent.getDefaultState();
        return switch (type) {
            case AWAY -> parentState.with(SlabBlock.TYPE, SlabType.BOTTOM);
            case TOWARDS -> parentState.with(SlabBlock.TYPE, SlabType.TOP);
            case FULL -> parentState.with(SlabBlock.TYPE, SlabType.DOUBLE);
        };
    }

    public static Identifier makeModelId(BlockState state) {
        verifyVertical(state.getBlock());
        return FullSlabs.id("block/%s/%s_%s".formatted(Registries.BLOCK.getId(state.getBlock()).getPath(), state.get(VerticalSlabBlock.DIRECTION).asString(), state.get(VerticalSlabBlock.TYPE).asString()));
    }

    @Override
    public BlockStateModel bake(BlockState state, Baker baker) {
        var slab = verifyVertical(state.getBlock());
        var parentId = Registries.BLOCK.getId(slab.parent);
        var templateId = templateId(state);
        var parentState = parentState(state);
        var outer = ((BakerImplOuterAccessor) baker).fullslabs$getOuter();
        var grouped = ((ModelBakerAccessor) outer).fullslabs$getBlockModels().get(parentState);
        if (grouped == null) throw new IllegalStateException("Parent slab state wasn't discovered: " + parentState);
        var parent = grouped.bake(parentState, baker);
        var particle = parent.particleSprite();
        var parts = parent.getParts(Random.create(0));
        var list = parts.stream().<HasQuads>map(x -> x::getQuads).toList();
        var useAO = parts.stream().findFirst().map(BlockModelPart::useAmbientOcclusion).orElse(true);
        var textures = Textures.fetch(particle, list);
        var mapped = mapped(parentId.toString(), textures);
        var template = baker.getModel(templateId);
        var geometry = template.getGeometry();
        var quads = geometry.bake(mapped, baker, ModelRotation.X0_Y0, template);
        var part = new GeometryBakedModel(quads, useAO, particle);
        return new SimpleBlockStateModel(part);
    }

    @Override
    public Object getEqualityGroup(BlockState state) {
        var id = Registries.BLOCK.getId(state.getBlock());
        var facing = state.get(VerticalSlabBlock.DIRECTION).asString();
        var type = state.get(VerticalSlabBlock.TYPE).asString();
        return id + "|" + facing + "|" + type;
    }

    @Override
    public void resolve(Resolver resolver) {
        TEMPLATES.forEach(resolver::markDependency);
    }

    private static List<Identifier> templates() {
        var builder = new ImmutableList.Builder<Identifier>();
        Direction.Type.HORIZONTAL.forEach(direction -> {
            var str = direction.asString();
            builder.add(FullSlabs.id("block/vertical/tilted/%s_towards".formatted(str)));
            builder.add(FullSlabs.id("block/vertical/tilted/%s_away".formatted(str)));
            builder.add(FullSlabs.id("block/vertical/tilted/%s_full".formatted(str)));
            builder.add(FullSlabs.id("block/vertical/normal/%s".formatted(str)));
        });
        builder.add(FullSlabs.id("block/vertical/normal/full"));
        return builder.build();
    }

    @FunctionalInterface
    public interface HasQuads {
        List<BakedQuad> getQuads(@Nullable Direction direction);
    }

    public static ModelTextures mapped(String simple, Textures textures) {
        return mapped(simple, textures.particle, textures.side, textures.top, textures.bottom);
    }

    public static ModelTextures mapped(String simple, Sprite particle, Sprite side, Sprite top, Sprite bottom) {
        var table = new ModelTextures.Textures.Builder()
                .addSprite("side", spriteId(side))
                .addSprite("top", spriteId(top))
                .addSprite("bottom", spriteId(bottom))
                .addSprite("particle", spriteId(particle))
                .build();
        return new ModelTextures.Builder().addLast(table).build(() -> simple);
    }

    private static SpriteIdentifier spriteId(Sprite sprite) {
        return new SpriteIdentifier(ATLAS, sprite.getContents().getId());
    }

    public record Textures(Sprite particle, Sprite side, Sprite top, Sprite bottom) {
        public static Textures fetch(Sprite particle, List<HasQuads> quads) {
            var side = Direction.Type.HORIZONTAL.stream()
                    .map(dir -> fetchFace(dir, quads))
                    .filter(Optional::isPresent).map(Optional::get)
                    .findFirst().orElse(particle);
            var top = fetchFace(Direction.UP, quads).orElse(side);
            var bottom = fetchFace(Direction.DOWN, quads).orElse(side);
            return new Textures(particle, side, top, bottom);
        }

        public static Optional<Sprite> fetchFace(Direction direction, List<HasQuads> parts) {
            return parts.stream().map(hasQuads -> fetchFace(direction, hasQuads)).findFirst().filter(Optional::isPresent).map(Optional::get).stream().findFirst();
        }

        public static Optional<Sprite> fetchFace(Direction direction, HasQuads parent) {
            return parent.getQuads(direction).stream().map(BakedQuad::sprite).findFirst().or(() -> parent.getQuads(null).stream().filter(q -> q.face() == direction).map(BakedQuad::sprite).findFirst());
        }
    }
}
