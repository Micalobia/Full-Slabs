package dev.micalobia.fullslabs.client.models;

import dev.micalobia.fullslabs.FullSlabs;
import dev.micalobia.fullslabs.VerticalSlabBlock;
import dev.micalobia.fullslabs.VerticalSlabBlock.VerticalType;
import dev.micalobia.fullslabs.config.ConfigManager;
import dev.micalobia.fullslabs.mixin.client.BakerImplOuterAccessor;
import dev.micalobia.fullslabs.mixin.client.ModelBakerAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
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
import java.util.stream.Stream;

public class VerticalSlabModel implements BlockStateModel.UnbakedGrouped {
    private final SlabBlock parent;
    @SuppressWarnings("deprecation")
    private static final Identifier ATLAS = SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;

    public VerticalSlabModel(VerticalSlabBlock block) {
        this.parent = block.parent;
    }

    public static Identifier makeModelId(BlockState state) {
        if (!(state.getBlock() instanceof VerticalSlabBlock)) throw new RuntimeException("Not a vertical slab!");
        return FullSlabs.id(String.format("block/%s/%s_%s", Registries.BLOCK.getId(state.getBlock()).getPath(), state.get(VerticalSlabBlock.DIRECTION).asString(), state.get(VerticalSlabBlock.TYPE).asString()));
    }

    @Override
    public BlockStateModel bake(BlockState state, Baker baker) {
        var cfg = ConfigManager.get();
        var facing = state.get(VerticalSlabBlock.DIRECTION);
        var type = state.get(VerticalSlabBlock.TYPE);
        var tilted = cfg.isTilted(this.parent);
        Identifier templateId;
        if (tilted)
            templateId = FullSlabs.id(String.format("block/vertical/tilted/%s_%s", facing.asString(), type.asString()));
        else if (type == VerticalType.FULL) templateId = FullSlabs.id("block/vertical/normal/full");
        else
            templateId = FullSlabs.id(String.format("block/vertical/normal/%s", (type == VerticalType.AWAY ? facing.getOpposite() : facing).asString()));
        var parentState = this.parent.getDefaultState();
        var outer = ((BakerImplOuterAccessor) baker).fullslabs$getOuter();
        var grouped = ((ModelBakerAccessor) outer).fullslabs$getBlockModels().get(parentState);
        if (grouped == null) throw new IllegalStateException("Parent slab state wasn't discovered: " + parentState);
        var parent = grouped.bake(parentState, baker);
        var particle = parent.particleSprite();
        var parts = parent.getParts(Random.create(0));
        var list = parts.stream().<HasQuads>map(x -> x::getQuads).toList();
        var useAO = parts.stream().findFirst().map(BlockModelPart::useAmbientOcclusion).orElse(true);
        var textures = Textures.fetch(particle, list);
        var mapped = mapped(textures);
        var template = baker.getModel(templateId);
        var geometry = template.getGeometry();
        var quads = geometry.bake(mapped, baker, ModelRotation.X0_Y0, template);
        var part = new GeometryBakedModel(quads, useAO, particle);
        return new SimpleBlockStateModel(part);
    }

    @Override
    public Object getEqualityGroup(BlockState state) {
        var id = Registries.BLOCK.getId(this.parent);
        var facing = state.get(VerticalSlabBlock.DIRECTION).asString();
        var type = state.get(VerticalSlabBlock.TYPE).asString();
        return id + "|" + facing + "|" + type;
    }

    @Override
    public void resolve(Resolver resolver) {
        Direction.Type.HORIZONTAL.forEach(direction -> {
            var str = direction.asString();
            resolver.markDependency(FullSlabs.id(String.format("block/vertical/tilted/%s_towards", str)));
            resolver.markDependency(FullSlabs.id(String.format("block/vertical/tilted/%s_away", str)));
            resolver.markDependency(FullSlabs.id(String.format("block/vertical/tilted/%s_full", str)));
            resolver.markDependency(FullSlabs.id(String.format("block/vertical/normal/%s", str)));
        });
        resolver.markDependency(FullSlabs.id("block/vertical/normal/full"));
    }

    @FunctionalInterface
    private interface HasQuads {
        List<BakedQuad> getQuads(@Nullable Direction direction);
    }

    private ModelTextures mapped(Textures textures) {
        return mapped(textures.particle, textures.side, textures.top, textures.bottom);
    }

    private ModelTextures mapped(Sprite particle, Sprite side, Sprite top, Sprite bottom) {
        var table = new ModelTextures.Textures.Builder()
                .addSprite("side", spriteId(side))
                .addSprite("top", spriteId(top))
                .addSprite("bottom", spriteId(bottom))
                .addSprite("particle", spriteId(particle))
                .build();
        final var str = Registries.BLOCK.getId(this.parent).toString();
        return new ModelTextures.Builder().addLast(table).build(() -> str);
    }

    private static SpriteIdentifier spriteId(Sprite sprite) {
        return new SpriteIdentifier(ATLAS, sprite.getContents().getId());
    }

    private record Textures(Sprite particle, Sprite side, Sprite top, Sprite bottom) {
        public static Textures fetch(Sprite particle, List<HasQuads> quads) {
            var side = Stream.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)
                    .map(dir -> fetchFace(dir, quads))
                    .filter(Optional::isPresent).map(Optional::get)
                    .findFirst().orElse(particle);
            var top = fetchFace(Direction.UP, quads).orElse(side);
            var bottom = fetchFace(Direction.DOWN, quads).orElse(side);
            return new Textures(particle, side, top, bottom);
        }

        private static Optional<Sprite> fetchFace(Direction direction, List<HasQuads> parts) {
            return parts.stream().map(hasQuads -> fetchFace(direction, hasQuads)).findFirst().filter(Optional::isPresent).map(Optional::get).stream().findFirst();
        }

        private static Optional<Sprite> fetchFace(Direction direction, HasQuads parent) {
            return parent.getQuads(direction).stream().map(BakedQuad::sprite).findFirst().or(() -> parent.getQuads(null).stream().filter(q -> q.face() == direction).map(BakedQuad::sprite).findFirst());
        }
    }
}
