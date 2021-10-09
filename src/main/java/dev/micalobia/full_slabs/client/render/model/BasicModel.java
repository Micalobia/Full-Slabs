package dev.micalobia.full_slabs.client.render.model;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.MaterialFinder;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext.QuadTransform;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
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
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class BasicModel implements UnbakedModel, BakedModel, FabricBakedModel {
	protected static final SpriteIdentifier MISSINGNO_ID = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("minecraft:builtin/missing"));
	private static final boolean hasCanvas = FabricLoader.getInstance().isModLoaded("canvas");
	private static Renderer cachedRenderer = null;
	private static MaterialFinder cachedMaterialFinder = null;
	private static BlockRenderManager cachedBlockRenderManager = null;
	protected Sprite missingParticle;

	protected static Renderer getRenderer() {
		if(cachedRenderer == null)
			cachedRenderer = RendererAccess.INSTANCE.getRenderer();
		return cachedRenderer;
	}

	protected static MaterialFinder getMaterialFinder() {
		if(cachedMaterialFinder == null)
			cachedMaterialFinder = getRenderer().materialFinder();
		return cachedMaterialFinder.clear();
	}

	protected static BlockRenderManager getBlockRenderManager() {
		if(cachedBlockRenderManager == null)
			cachedBlockRenderManager = MinecraftClient.getInstance().getBlockRenderManager();
		return cachedBlockRenderManager;
	}

	protected static QuadTransform applyMaterial(RenderMaterial material) {
		return quad -> {
			quad.material(material);
			return true;
		};
	}

	protected static void emitFabricModel(FabricBakedModel model, QuadTransform transform, BlockRenderView view, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
		context.pushTransform(transform);
		model.emitBlockQuads(view, state, pos, randomSupplier, context);
		context.popTransform();
	}

	protected static void emitModel(BlockRenderView view, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
		BakedModel model = getBlockRenderManager().getModel(state);
		FabricBakedModel fmodel = (FabricBakedModel) model;
		RenderLayer layer = RenderLayers.getBlockLayer(state);
		BlendMode blendMode = BlendMode.fromRenderLayer(layer);
		RenderMaterial material = getMaterialFinder().blendMode(0, blendMode).find();
		if(hasCanvas)
			emitFabricModel(fmodel, applyMaterial(material), view, state, pos, randomSupplier, context);
		else if(fmodel.isVanillaAdapter()) {
			QuadEmitter emitter = context.getEmitter();
			for(Direction direction : Direction.values())
				for(BakedQuad quad : model.getQuads(state, direction, randomSupplier.get())) {
					emitter.fromVanilla(quad, material, direction);
					emitter.emit();
				}
			for(BakedQuad quad : model.getQuads(state, null, randomSupplier.get())) {
				emitter.fromVanilla(quad, material, null);
				emitter.emit();
			}
		} else emitFabricModel(fmodel, applyMaterial(material), view, state, pos, randomSupplier, context);
	}

	@Override
	public Collection<Identifier> getModelDependencies() {
		return Collections.emptySet();
	}

	@Override
	public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
		return Collections.emptySet();
	}

	@Nullable
	@Override
	public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
		missingParticle = textureGetter.apply(MISSINGNO_ID);
		return this;
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
		return null;
	}

	@Override
	public boolean useAmbientOcclusion() {
		return true;
	}

	@Override
	public boolean hasDepth() {
		return false;
	}

	@Override
	public boolean isSideLit() {
		return false;
	}

	@Override
	public boolean isBuiltin() {
		return false;
	}

	@Override
	public Sprite getParticleSprite() {
		return missingParticle;
	}

	@Override
	public ModelTransformation getTransformation() {
		return null;
	}

	@Override
	public ModelOverrideList getOverrides() {
		return null;
	}

	@Override
	public boolean isVanillaAdapter() {
		return false;
	}

	@Override
	public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {

	}
}

