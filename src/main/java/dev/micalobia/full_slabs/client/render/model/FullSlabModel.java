package dev.micalobia.full_slabs.client.render.model;

import com.mojang.datafixers.util.Pair;
import dev.micalobia.full_slabs.block.FullSlabBlock;
import dev.micalobia.full_slabs.block.VerticalSlabBlock;
import dev.micalobia.full_slabs.util.Helper;
import dev.micalobia.full_slabs.util.TiltedSlabs;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.BlockRenderView;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class FullSlabModel implements BakedModel, UnbakedModel, FabricBakedModel {
	private static final SpriteIdentifier MISSINGNO_ID = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("minecraft:builtin/missing"));
	private static final Map<Triple<Axis, Block, Block>, Mesh> cachedMeshes = new HashMap<>();
	private Sprite missingParticle;

	public FullSlabModel() {
	}

	public static void clearCachedMeshes() {
		cachedMeshes.clear();
	}

	private static Sprite getSpriteFromFace(Direction face, BlockState state, BakedModel model) {
		return ((IBakedQuad) (model.getQuads(state, face, null).get(0))).getSprite();
	}

	private static QuadEmitter finalize(QuadEmitter emitter, Sprite sprite, int uv) {
		return emitter.spriteBake(0, sprite, uv)
				.spriteColor(0, -1, -1, -1, -1)
				.emit();
	}

	private static QuadEmitter fullSquare(QuadEmitter emitter, Direction from) {
		return emitter.square(from, 0f, 0f, 1f, 1f, 0f);
	}

	private static QuadEmitter leftSquare(QuadEmitter emitter, Direction from) {
		return emitter.square(from, 0f, 0f, 0.5f, 1f, 0f);
	}

	private static QuadEmitter rightSquare(QuadEmitter emitter, Direction from) {
		return emitter.square(from, 0.5f, 0f, 1f, 1f, 0f);
	}

	private static QuadEmitter topSquare(QuadEmitter emitter, Direction from) {
		return emitter.square(from, 0f, 0.5f, 1f, 1f, 0f);
	}

	private static QuadEmitter bottomSquare(QuadEmitter emitter, Direction from) {
		return emitter.square(from, 0f, 0f, 1f, 0.5f, 0f);
	}

	private static int getUV(BlockState state, Direction from, Axis axis) {
		VerticalSlabBlock block = (VerticalSlabBlock) state.getBlock();
		if(!TiltedSlabs.isDouble(block)) return MutableQuadView.BAKE_LOCK_UV;
		if(axis == Axis.X) {
			return MutableQuadView.BAKE_LOCK_UV |
					MutableQuadView.BAKE_ROTATE_90 |
					(from == Direction.NORTH ? MutableQuadView.BAKE_FLIP_U : MutableQuadView.BAKE_FLIP_V);
		}
		switch(from) {
			case UP:
				return MutableQuadView.BAKE_LOCK_UV | MutableQuadView.BAKE_ROTATE_180;
			case DOWN:
				return MutableQuadView.BAKE_LOCK_UV;
			case EAST:
				return MutableQuadView.BAKE_LOCK_UV | MutableQuadView.BAKE_ROTATE_270;
			default:
				return MutableQuadView.BAKE_LOCK_UV | MutableQuadView.BAKE_ROTATE_90 | MutableQuadView.BAKE_FLIP_V;
		}
	}

	public Collection<Identifier> getModelDependencies() {
		return Collections.emptyList();
	}

	public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
		return Collections.emptyList();
	}

	@Nullable
	public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
		missingParticle = textureGetter.apply(MISSINGNO_ID);
		return this;
	}

	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
		return null;
	}

	public boolean useAmbientOcclusion() {
		return true;
	}

	public boolean hasDepth() {
		return false;
	}

	public boolean isSideLit() {
		return false;
	}

	public boolean isBuiltin() {
		return false;
	}

	public Sprite getSprite() {
		return missingParticle;
	}

	public ModelTransformation getTransformation() {
		return null;
	}

	public ModelOverrideList getOverrides() {
		return null;
	}

	public boolean isVanillaAdapter() {
		return false;
	}

	public void emitBlockQuads(BlockRenderView blockRenderView, BlockState blockState, BlockPos blockPos, Supplier<Random> supplier, RenderContext renderContext) {
		//FullSlabBlockEntity entity = (FullSlabBlockEntity) blockRenderView.getBlockEntity(blockPos);
		RenderAttachedBlockView view = (RenderAttachedBlockView) blockRenderView;
		renderContext.meshConsumer().accept(getOrCreateMesh(blockState, (Pair<Block, Block>) view.getBlockEntityRenderAttachment(blockPos)));
	}

	public void emitItemQuads(ItemStack itemStack, Supplier<Random> supplier, RenderContext renderContext) {

	}

	private Mesh getOrCreateMesh(BlockState state, Pair<Block, Block> slabs) {
		//if(entity == null) return RendererAccess.INSTANCE.getRenderer().meshBuilder().build();
		Triple<Axis, Block, Block> key = Triple.of(state.get(FullSlabBlock.AXIS), slabs.getFirst(), slabs.getSecond());
		Mesh ret = cachedMeshes.get(key);
		if(ret == null) cachedMeshes.put(key, ret = createMesh(state, slabs));
		return ret;
	}

	private Mesh createMesh(BlockState state, Pair<Block, Block> slabs) {
		Axis axis = state.get(FullSlabBlock.AXIS);
		Block positiveSlab = slabs.getFirst();  // entity.getPositiveSlab();
		Block negativeSlab = slabs.getSecond(); // entity.getNegativeSlab();
		BlockRenderManager manager = MinecraftClient.getInstance().getBlockRenderManager();
		BlockState positiveState = positiveSlab.getDefaultState().with(SlabBlock.TYPE, SlabType.BOTTOM);
		BlockState negativeState = negativeSlab.getDefaultState().with(SlabBlock.TYPE, SlabType.BOTTOM);

		BakedModel positiveModel = manager.getModel(positiveState);
		BakedModel negativeModel = manager.getModel(negativeState);
		final int top = 0;
		final int bottom = 1;
		final int side = 2;

		Sprite[] positiveSprites = new Sprite[]{
				getSpriteFromFace(null, positiveState, positiveModel),
				getSpriteFromFace(Direction.DOWN, positiveState, positiveModel),
				getSpriteFromFace(Direction.NORTH, positiveState, positiveModel)
		};

		Sprite[] negativeSprites = new Sprite[]{
				getSpriteFromFace(null, negativeState, negativeModel),
				getSpriteFromFace(Direction.DOWN, negativeState, negativeModel),
				getSpriteFromFace(Direction.NORTH, negativeState, negativeModel)
		};

		BlockState cachedPositive = Helper.getState(positiveSlab, axis, true);
		BlockState cachedNegative = Helper.getState(negativeSlab, axis, false);

		Renderer renderer = RendererAccess.INSTANCE.getRenderer();
		MeshBuilder builder = renderer.meshBuilder();
		QuadEmitter emitter = builder.getEmitter();
		switch(axis) {
			case X: {
				boolean positiveTilted = TiltedSlabs.isSingle(positiveSlab);
				finalize(fullSquare(emitter, Direction.EAST), positiveSprites[positiveTilted ? top : side], MutableQuadView.BAKE_LOCK_UV);
				finalize(rightSquare(emitter, Direction.SOUTH), positiveSprites[side], getUV(cachedPositive, Direction.SOUTH, axis));
				finalize(rightSquare(emitter, Direction.UP), positiveSprites[positiveTilted ? side : top], getUV(cachedPositive, Direction.UP, axis));
				finalize(rightSquare(emitter, Direction.DOWN), positiveSprites[positiveTilted ? side : bottom], getUV(cachedPositive, Direction.DOWN, axis));
				finalize(leftSquare(emitter, Direction.NORTH), positiveSprites[side], getUV(cachedPositive, Direction.NORTH, axis));

				boolean negativeTilted = TiltedSlabs.isSingle(negativeSlab);
				finalize(fullSquare(emitter, Direction.WEST), negativeSprites[negativeTilted ? bottom : side], MutableQuadView.BAKE_LOCK_UV);
				finalize(leftSquare(emitter, Direction.SOUTH), negativeSprites[side], getUV(cachedNegative, Direction.SOUTH, axis));
				finalize(leftSquare(emitter, Direction.UP), negativeSprites[negativeTilted ? side : top], getUV(cachedNegative, Direction.UP, axis));
				finalize(leftSquare(emitter, Direction.DOWN), negativeSprites[negativeTilted ? side : bottom], getUV(cachedNegative, Direction.DOWN, axis));
				finalize(rightSquare(emitter, Direction.NORTH), negativeSprites[side], getUV(cachedNegative, Direction.NORTH, axis));
				break;
			}
			case Z: {
				boolean positiveTilted = TiltedSlabs.isSingle(positiveSlab);
				finalize(fullSquare(emitter, Direction.SOUTH), positiveSprites[positiveTilted ? top : side], MutableQuadView.BAKE_LOCK_UV);
				finalize(leftSquare(emitter, Direction.EAST), positiveSprites[side], getUV(cachedPositive, Direction.EAST, axis));
				finalize(rightSquare(emitter, Direction.WEST), positiveSprites[side], getUV(cachedPositive, Direction.WEST, axis));
				finalize(bottomSquare(emitter, Direction.UP), positiveSprites[positiveTilted ? side : top], getUV(cachedPositive, Direction.UP, axis));
				finalize(topSquare(emitter, Direction.DOWN), positiveSprites[positiveTilted ? side : bottom], getUV(cachedPositive, Direction.DOWN, axis));

				boolean negativeTilted = TiltedSlabs.isSingle(negativeSlab);
				finalize(fullSquare(emitter, Direction.NORTH), negativeSprites[negativeTilted ? bottom : side], MutableQuadView.BAKE_LOCK_UV);
				finalize(rightSquare(emitter, Direction.EAST), negativeSprites[side], getUV(cachedNegative, Direction.EAST, axis));
				finalize(leftSquare(emitter, Direction.WEST), negativeSprites[side], getUV(cachedNegative, Direction.WEST, axis));
				finalize(topSquare(emitter, Direction.UP), negativeSprites[negativeTilted ? side : top], getUV(cachedNegative, Direction.UP, axis));
				finalize(bottomSquare(emitter, Direction.DOWN), negativeSprites[negativeTilted ? side : bottom], getUV(cachedNegative, Direction.DOWN, axis));
				break;
			}
			default:
				finalize(fullSquare(emitter, Direction.UP), positiveSprites[top], MutableQuadView.BAKE_LOCK_UV);
				finalize(topSquare(emitter, Direction.NORTH), positiveSprites[side], MutableQuadView.BAKE_LOCK_UV);
				finalize(topSquare(emitter, Direction.EAST), positiveSprites[side], MutableQuadView.BAKE_LOCK_UV);
				finalize(topSquare(emitter, Direction.SOUTH), positiveSprites[side], MutableQuadView.BAKE_LOCK_UV);
				finalize(topSquare(emitter, Direction.WEST), positiveSprites[side], MutableQuadView.BAKE_LOCK_UV);

				finalize(fullSquare(emitter, Direction.DOWN), negativeSprites[bottom], MutableQuadView.BAKE_LOCK_UV);
				finalize(bottomSquare(emitter, Direction.NORTH), negativeSprites[side], MutableQuadView.BAKE_LOCK_UV);
				finalize(bottomSquare(emitter, Direction.EAST), negativeSprites[side], MutableQuadView.BAKE_LOCK_UV);
				finalize(bottomSquare(emitter, Direction.SOUTH), negativeSprites[side], MutableQuadView.BAKE_LOCK_UV);
				finalize(bottomSquare(emitter, Direction.WEST), negativeSprites[side], MutableQuadView.BAKE_LOCK_UV);
				break;
		}
		return builder.build();
	}
}
