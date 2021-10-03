package dev.micalobia.full_slabs.client.render.model;

import com.mojang.datafixers.util.Pair;
import dev.micalobia.full_slabs.FullSlabsMod;
import dev.micalobia.full_slabs.block.FullSlabBlock;
import dev.micalobia.full_slabs.util.Utility;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.MaterialFinder;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext.QuadTransform;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.block.Block;
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
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class FullSlabModel implements UnbakedModel, BakedModel, FabricBakedModel {
	private static final SpriteIdentifier MISSINGNO_ID = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("minecraft:builtin/missing"));
	private Sprite missingParticle;

	public FullSlabModel() {
	}

	private static QuadTransform applyMaterial(RenderMaterial material) {
		return (quad -> {
			quad.material(material);
			FullSlabsMod.LOGGER.info(quad.getClass());
			return true;
		});
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

	@SuppressWarnings("unchecked")
	@Override
	public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
		RenderAttachedBlockView view = (RenderAttachedBlockView) blockView;
		Pair<Block, Block> pair = (Pair<Block, Block>) view.getBlockEntityRenderAttachment(pos);
		Axis axis = state.get(FullSlabBlock.AXIS);
		BlockState positiveState = Utility.getSlabState(pair, axis, true);
		BlockState negativeState = Utility.getSlabState(pair, axis, false);
		BlockRenderManager manager = MinecraftClient.getInstance().getBlockRenderManager();
		var renderer = RendererAccess.INSTANCE.getRenderer();
		assert renderer != null;
		MaterialFinder finder = renderer.materialFinder();
		RenderLayer positiveLayer = RenderLayers.getBlockLayer(positiveState);
		RenderLayer negativeLayer = RenderLayers.getBlockLayer(negativeState);
		BlendMode positiveBlend = BlendMode.fromRenderLayer(positiveLayer);
		BlendMode negativeBlend = BlendMode.fromRenderLayer(negativeLayer);
		RenderMaterial positiveMaterial = finder.clear().blendMode(0, positiveBlend).find();
		RenderMaterial negativeMaterial = finder.clear().blendMode(0, negativeBlend).find();
		BakedModel positiveModel = manager.getModel(positiveState);
		BakedModel negativeModel = manager.getModel(negativeState);
		MeshBuilder builder = renderer.meshBuilder();
		QuadEmitter quadEmitter = builder.getEmitter();
//		context.pushTransform(applyMaterial(finder.clear().find()));
//		context.fallbackConsumer().accept(positiveModel);
//		context.popTransform();
//		context.pushTransform(applyMaterial(finder.clear().find()));
//		context.fallbackConsumer().accept(negativeModel);
//		context.popTransform();
		//((FabricBakedModel) positiveModel).emitBlockQuads(blockView, state, pos, randomSupplier, context);
		//((FabricBakedModel) negativeModel).emitBlockQuads(blockView, state, pos, randomSupplier, context);
		boolean positiveCull = positiveBlend != BlendMode.SOLID && blockView.getBlockState(pos.offset(axis, 1)).isOf(FullSlabsMod.FULL_SLAB_BLOCK);
		boolean negativeCull = negativeBlend != BlendMode.SOLID && blockView.getBlockState(pos.offset(axis, -1)).isOf(FullSlabsMod.FULL_SLAB_BLOCK);
		for(Direction direction : Direction.values()) {
			for(BakedQuad quad : positiveModel.getQuads(positiveState, direction, randomSupplier.get())) {
				quadEmitter.fromVanilla(quad, positiveMaterial, positiveCull ? direction : null);
				quadEmitter.emit();
			}
			for(BakedQuad quad : negativeModel.getQuads(negativeState, direction, randomSupplier.get())) {
				quadEmitter.fromVanilla(quad, negativeMaterial, negativeCull ? direction : null);
				quadEmitter.emit();
			}
		}
		for(BakedQuad quad : positiveModel.getQuads(positiveState, null, randomSupplier.get())) {
			quadEmitter.fromVanilla(quad, positiveMaterial, null);
			quadEmitter.emit();
		}
		for(BakedQuad quad : negativeModel.getQuads(negativeState, null, randomSupplier.get())) {
			quadEmitter.fromVanilla(quad, negativeMaterial, null);
			quadEmitter.emit();
		}
		context.meshConsumer().accept(builder.build());
//		context.fallbackConsumer().accept((BakedModel) positiveModel);
//		context.fallbackConsumer().accept((BakedModel) negativeModel);
		//positiveModel.emitBlockQuads(blockView, state, pos, randomSupplier, context);
		//negativeModel.emitBlockQuads(blockView, state, pos, randomSupplier, context);
		//context.popTransform();
	}

	@Override
	public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {

	}
}
