package dev.micalobia.full_slabs.client.render.model;

import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Pair.Mu;
import dev.micalobia.full_slabs.block.VerticalSlabBlock;
import dev.micalobia.full_slabs.block.enums.SlabState;
import dev.micalobia.full_slabs.util.Helper;
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
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class VerticalSlabModel implements FabricBakedModel, BakedModel, UnbakedModel {
	private final SlabBlock base;
	private final BlockState cachedState;
	private Sprite particle;
	private Mesh mesh;

	public VerticalSlabModel(ModelIdentifier id) {
		String path = id.getPath();
		int i = path.indexOf('_');
		Identifier baseId = new Identifier(path.substring(0, i), path.substring(i + 1, path.length() - "_vertical".length()));
		this.base = (SlabBlock) Helper.fetchBlock(baseId);
		String[] states = id.getVariant().split(",");
		Axis axis = Axis.X;
		SlabState slabState = SlabState.DOUBLE;
		for(String state : states) {
			String[] pairs = state.split("=");
			String value = pairs[1];
			switch(value) {
				case "x":
					axis = Axis.X;
					break;
				case "z":
					axis = Axis.Z;
					break;
				case "positive":
					slabState = SlabState.POSITIVE;
					break;
				case "negative":
					slabState = SlabState.NEGATIVE;
					break;
				case "double":
					slabState = SlabState.DOUBLE;
					break;
			}
		}
		this.cachedState = Helper.fetchDefaultState(new Identifier(id.getNamespace(), id.getPath())).with(VerticalSlabBlock.AXIS, axis).with(VerticalSlabBlock.STATE, slabState);
	}

	private static int getUV(BlockState state, Direction from, Axis axis) {
		VerticalSlabBlock block = (VerticalSlabBlock) state.getBlock();
		if(!block.tiltable) return MutableQuadView.BAKE_LOCK_UV;
		if(axis == Axis.X) {
			return MutableQuadView.BAKE_LOCK_UV |
					MutableQuadView.BAKE_ROTATE_90 |
					(from == Direction.NORTH ? MutableQuadView.BAKE_FLIP_U : MutableQuadView.BAKE_FLIP_V);
		}
		switch(from) {
			case UP: return MutableQuadView.BAKE_LOCK_UV | MutableQuadView.BAKE_ROTATE_180;
			case DOWN: return MutableQuadView.BAKE_LOCK_UV;
			case EAST: return MutableQuadView.BAKE_LOCK_UV | MutableQuadView.BAKE_ROTATE_270;
			default: return MutableQuadView.BAKE_LOCK_UV | MutableQuadView.BAKE_ROTATE_90 | MutableQuadView.BAKE_FLIP_V;
		}
	}

	private static QuadEmitter finalize(QuadEmitter emitter, Sprite sprite, int uv) {
		return emitter.spriteBake(0, sprite, uv)
				.spriteColor(0, -1, -1, -1, -1)
				.emit();
	}

	private static QuadEmitter fullSquare(QuadEmitter emitter, Direction from, float depth) {
		return emitter.square(from, 0f, 0f, 1f, 1f, depth);
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

	public Collection<Identifier> getModelDependencies() {
		return Collections.emptyList();
	}

	public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
		return Collections.emptyList();
	}

	@Nullable
	public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
		//particle = textureGetter.apply(MISSINGNO_ID);
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
		if(mesh == null) mesh = createMesh();
		return particle;
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
		if(mesh == null) mesh = createMesh();
		renderContext.meshConsumer().accept(mesh);
	}

	public void emitItemQuads(ItemStack itemStack, Supplier<Random> supplier, RenderContext renderContext) {

	}

	private Mesh createMesh() {
		Renderer renderer = RendererAccess.INSTANCE.getRenderer();
		MeshBuilder builder = renderer.meshBuilder();
		try {
			BlockRenderManager manager = MinecraftClient.getInstance().getBlockRenderManager();
			BlockState baseState = base.getDefaultState().with(SlabBlock.TYPE, SlabType.BOTTOM);
			BakedModel model = manager.getModel(baseState);

			QuadEmitter emitter = builder.getEmitter();
			SlabState slabState = cachedState.get(VerticalSlabBlock.STATE);
			Axis axis = cachedState.get(VerticalSlabBlock.AXIS);
			Sprite topSprite = ((IBakedQuad) model.getQuads(baseState, null, null).get(0)).getSprite();
			Sprite bottomSprite = ((IBakedQuad) model.getQuads(baseState, Direction.DOWN, null).get(0)).getSprite();
			Sprite sideSprite = ((IBakedQuad) model.getQuads(baseState, Direction.NORTH, null).get(0)).getSprite();
			particle = sideSprite;

			boolean tilted = ((VerticalSlabBlock) cachedState.getBlock()).tiltable;
			if(slabState == SlabState.DOUBLE) {
				if (tilted) {
					boolean x = axis == Axis.X;
					boolean z = axis == Axis.Z;
					finalize(fullSquare(emitter, Direction.UP, 0f), sideSprite, getUV(cachedState, Direction.UP, axis));
					finalize(fullSquare(emitter, Direction.DOWN, 0f), sideSprite, getUV(cachedState, Direction.DOWN, axis));
					finalize(fullSquare(emitter, Direction.EAST, 0f), x ? topSprite : sideSprite, x ? MutableQuadView.BAKE_LOCK_UV : getUV(cachedState, Direction.EAST, axis));
					finalize(fullSquare(emitter, Direction.WEST, 0f), x ? bottomSprite : sideSprite, x ? MutableQuadView.BAKE_LOCK_UV : getUV(cachedState, Direction.WEST, axis));
					finalize(fullSquare(emitter, Direction.NORTH, 0f), z ? bottomSprite : sideSprite, z ? MutableQuadView.BAKE_LOCK_UV : getUV(cachedState, Direction.NORTH, axis));
					finalize(fullSquare(emitter, Direction.SOUTH, 0f), z ? topSprite : sideSprite, z ? MutableQuadView.BAKE_LOCK_UV : getUV(cachedState, Direction.SOUTH, axis));
				}
				else {
					finalize(fullSquare(emitter, Direction.UP, 0f), topSprite, MutableQuadView.BAKE_LOCK_UV);
					finalize(fullSquare(emitter, Direction.DOWN, 0f), bottomSprite, MutableQuadView.BAKE_LOCK_UV);
					finalize(fullSquare(emitter, Direction.NORTH, 0f), sideSprite, MutableQuadView.BAKE_LOCK_UV);
					finalize(fullSquare(emitter, Direction.EAST, 0f), sideSprite, MutableQuadView.BAKE_LOCK_UV);
					finalize(fullSquare(emitter, Direction.SOUTH, 0f), sideSprite, MutableQuadView.BAKE_LOCK_UV);
					finalize(fullSquare(emitter, Direction.WEST, 0f), sideSprite, MutableQuadView.BAKE_LOCK_UV);
				}
			} else {
				if(slabState == SlabState.POSITIVE) {
					if(axis == Axis.X) {
						finalize(fullSquare(emitter, Direction.WEST, 0.5f), tilted ? bottomSprite : sideSprite, MutableQuadView.BAKE_LOCK_UV);
						finalize(fullSquare(emitter, Direction.EAST, 0f), tilted ? topSprite : sideSprite, MutableQuadView.BAKE_LOCK_UV);
						finalize(rightSquare(emitter, Direction.SOUTH), sideSprite, getUV(cachedState, Direction.SOUTH, axis));
						finalize(rightSquare(emitter, Direction.UP), tilted ? sideSprite : topSprite, getUV(cachedState, Direction.UP, axis));
						finalize(rightSquare(emitter, Direction.DOWN), tilted ? sideSprite : bottomSprite, getUV(cachedState, Direction.DOWN, axis));
						finalize(leftSquare(emitter, Direction.NORTH), sideSprite, getUV(cachedState, Direction.NORTH, axis));
					} else {
						finalize(fullSquare(emitter, Direction.NORTH, 0.5f), tilted ? bottomSprite : sideSprite, MutableQuadView.BAKE_LOCK_UV);
						finalize(fullSquare(emitter, Direction.SOUTH, 0f), tilted ? topSprite : sideSprite, MutableQuadView.BAKE_LOCK_UV);
						finalize(leftSquare(emitter, Direction.EAST), sideSprite, getUV(cachedState, Direction.EAST, axis));
						finalize(rightSquare(emitter, Direction.WEST), sideSprite, getUV(cachedState, Direction.WEST, axis));
						finalize(bottomSquare(emitter, Direction.UP), tilted ? sideSprite : topSprite, getUV(cachedState, Direction.UP, axis));
						finalize(topSquare(emitter, Direction.DOWN), tilted ? sideSprite : bottomSprite, getUV(cachedState, Direction.DOWN, axis));
					}
				} else {
					if(axis == Axis.X) {
						finalize(fullSquare(emitter, Direction.WEST, 0f), tilted ? bottomSprite : sideSprite, MutableQuadView.BAKE_LOCK_UV);
						finalize(fullSquare(emitter, Direction.EAST, 0.5f), tilted ? topSprite : sideSprite, MutableQuadView.BAKE_LOCK_UV);
						finalize(leftSquare(emitter, Direction.SOUTH), sideSprite, getUV(cachedState, Direction.SOUTH, axis));
						finalize(leftSquare(emitter, Direction.UP), tilted ? sideSprite : topSprite, getUV(cachedState, Direction.UP, axis));
						finalize(leftSquare(emitter, Direction.DOWN), tilted ? sideSprite : bottomSprite, getUV(cachedState, Direction.DOWN, axis));
						finalize(rightSquare(emitter, Direction.NORTH), sideSprite, getUV(cachedState, Direction.NORTH, axis));
					} else {
						finalize(fullSquare(emitter, Direction.NORTH, 0f), tilted ? bottomSprite : sideSprite, MutableQuadView.BAKE_LOCK_UV);
						finalize(fullSquare(emitter, Direction.SOUTH, 0.5f), tilted ? topSprite : sideSprite, MutableQuadView.BAKE_LOCK_UV);
						finalize(rightSquare(emitter, Direction.EAST), sideSprite, getUV(cachedState, Direction.EAST, axis));
						finalize(leftSquare(emitter, Direction.WEST), sideSprite, getUV(cachedState, Direction.WEST, axis));
						finalize(topSquare(emitter, Direction.UP), tilted ? sideSprite : topSprite, getUV(cachedState, Direction.UP, axis));
						finalize(bottomSquare(emitter, Direction.DOWN), tilted ? sideSprite : bottomSprite, getUV(cachedState, Direction.DOWN, axis));
					}
				}
			}


		} catch(Exception err) {
			err.printStackTrace();
		}
		return builder.build();
	}
}
