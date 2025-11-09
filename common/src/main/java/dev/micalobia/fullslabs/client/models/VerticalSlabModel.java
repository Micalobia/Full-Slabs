package dev.micalobia.fullslabs.client.models;

import com.google.common.collect.ImmutableList;
import dev.micalobia.fullslabs.FullSlabs;
import dev.micalobia.fullslabs.block.VerticalSlabBlock;
import dev.micalobia.fullslabs.block.VerticalSlabBlock.VerticalType;
import dev.micalobia.fullslabs.config.Config;
import dev.micalobia.fullslabs.mixin.client.ModelBakerImplAccessor;
import dev.micalobia.fullslabs.mixin.client.ModelBakeryAccessor;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

@MethodsReturnNonnullByDefault
public class VerticalSlabModel implements BlockStateModel.UnbakedRoot {
    @SuppressWarnings("deprecation")
    private static final ResourceLocation ATLAS = TextureAtlas.LOCATION_BLOCKS;
    public static final VerticalSlabModel INSTANCE = new VerticalSlabModel();

    public static final List<ResourceLocation> TEMPLATES = templates();

    private VerticalSlabModel() {
    }

    private static VerticalSlabBlock verifyVertical(Block block) {
        if (!(block instanceof VerticalSlabBlock slab)) throw new IllegalArgumentException();
        return slab;
    }

    public static ResourceLocation templateId(BlockState state) {
        var slab = verifyVertical(state.getBlock());
        var facing = state.getValue(VerticalSlabBlock.DIRECTION);
        var type = state.getValue(VerticalSlabBlock.TYPE);
        var tilted = Config.isTilted(slab.parent);
        if (tilted)
            return FullSlabs.id("block/vertical/tilted/%s_%s".formatted(facing.getSerializedName(), type.getSerializedName()));
        if (type == VerticalType.FULL) return FullSlabs.id("block/vertical/normal/full");
        return FullSlabs.id("block/vertical/normal/%s".formatted((type == VerticalType.AWAY ? facing.getOpposite() : facing).getSerializedName()));
    }

    public static BlockState parentState(BlockState state) {
        var slab = verifyVertical(state.getBlock());
        var type = state.getValue(VerticalSlabBlock.TYPE);
        var parentState = slab.parent.defaultBlockState();
        return switch (type) {
            case AWAY -> parentState.setValue(BlockStateProperties.SLAB_TYPE, SlabType.BOTTOM);
            case TOWARDS -> parentState.setValue(BlockStateProperties.SLAB_TYPE, SlabType.TOP);
            case FULL -> parentState.setValue(BlockStateProperties.SLAB_TYPE, SlabType.DOUBLE);
        };
    }

    public static ResourceLocation makeModelId(BlockState state) {
        var slab = verifyVertical(state.getBlock());
        return FullSlabs.id("block/%s/%s_%s".formatted(BuiltInRegistries.BLOCK.getKey(slab).getPath(), state.getValue(VerticalSlabBlock.DIRECTION).getSerializedName(), state.getValue(VerticalSlabBlock.TYPE).getSerializedName()));
    }

    @Override
    public BlockStateModel bake(BlockState state, ModelBaker baker) {
        var slab = verifyVertical(state.getBlock());
        var parentId = BuiltInRegistries.BLOCK.getKey(slab.parent);
        var templateId = templateId(state);
        var parentState = parentState(state);
        var outer = ((ModelBakerImplAccessor) baker).fullslabs$getOuter();
        var grouped = ((ModelBakeryAccessor) outer).fullslabs$getUnbakedBlockStateModels().get(parentState);
        if (grouped == null) throw new IllegalStateException("Parent slab state wasn't discovered: " + parentState);
        var parent = grouped.bake(parentState, baker);
        var particle = parent.particleIcon();
        var parts = parent.collectParts(RandomSource.create(0));
        var list = parts.stream().<HasQuads>map(x -> x::getQuads).toList();
        var useAO = parts.stream().findFirst().map(BlockModelPart::useAmbientOcclusion).orElse(true);
        var textures = Textures.fetch(particle, list);
        var mapped = mapped(parentId.toString(), textures);
        var template = baker.getModel(templateId);
        var geometry = template.getTopGeometry();
        var quads = geometry.bake(mapped, baker, BlockModelRotation.X0_Y0, template);
        var part = new SimpleModelWrapper(quads, useAO, particle);
        return new SingleVariant(part);
    }

    @Override
    public Object visualEqualityGroup(BlockState state) {
        var id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        var facing = state.getValue(VerticalSlabBlock.DIRECTION).getSerializedName();
        var type = state.getValue(VerticalSlabBlock.TYPE).getSerializedName();
        return id + "|" + facing + "|" + type;
    }

    @Override
    public void resolveDependencies(Resolver resolver) {
        TEMPLATES.forEach(resolver::markDependency);
    }

    private static List<ResourceLocation> templates() {
        var builder = new ImmutableList.Builder<ResourceLocation>();
        Direction.Plane.HORIZONTAL.forEach(direction -> {
            var str = direction.getSerializedName();
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

    public static TextureSlots mapped(String simple, Textures textures) {
        return mapped(simple, textures.particle, textures.side, textures.top, textures.bottom);
    }

    public static TextureSlots mapped(String simple, TextureAtlasSprite particle, TextureAtlasSprite side, TextureAtlasSprite top, TextureAtlasSprite bottom) {
        var table = new TextureSlots.Data.Builder()
                .addTexture("side", spriteId(side))
                .addTexture("top", spriteId(top))
                .addTexture("bottom", spriteId(bottom))
                .addTexture("particle", spriteId(particle))
                .build();
        return new TextureSlots.Resolver().addLast(table).resolve(() -> simple);
    }

    private static Material spriteId(TextureAtlasSprite sprite) {
        return new Material(ATLAS, sprite.contents().name());
    }

    public record Textures(TextureAtlasSprite particle, TextureAtlasSprite side, TextureAtlasSprite top,
                           TextureAtlasSprite bottom) {
        public static Textures fetch(TextureAtlasSprite particle, List<HasQuads> quads) {
            var side = Direction.Plane.HORIZONTAL.stream()
                    .map(dir -> fetchFace(dir, quads))
                    .filter(Optional::isPresent).map(Optional::get)
                    .findFirst().orElse(particle);
            var top = fetchFace(Direction.UP, quads).orElse(side);
            var bottom = fetchFace(Direction.DOWN, quads).orElse(side);
            return new Textures(particle, side, top, bottom);
        }

        public static Optional<TextureAtlasSprite> fetchFace(Direction direction, List<HasQuads> parts) {
            return parts.stream().map(hasQuads -> fetchFace(direction, hasQuads)).findFirst().filter(Optional::isPresent).map(Optional::get).stream().findFirst();
        }

        public static Optional<TextureAtlasSprite> fetchFace(Direction direction, HasQuads parent) {
            return parent.getQuads(direction).stream().map(BakedQuad::sprite).findFirst().or(() -> parent.getQuads(null).stream().filter(q -> q.direction() == direction).map(BakedQuad::sprite).findFirst());
        }
    }
}
