package dev.micalobia.full_slabs.client.render.model;

import com.mojang.datafixers.util.Pair;
import dev.micalobia.full_slabs.block.FullSlabBlock;
import dev.micalobia.full_slabs.util.Helper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
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
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class FullSlabModel implements BakedModel, UnbakedModel, FabricBakedModel {
	private static final SpriteIdentifier MISSINGNO_ID = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("minecraft:builtin/missing"));
	private Sprite missingParticle;

	public FullSlabModel() {
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
		RenderAttachedBlockView view = (RenderAttachedBlockView) blockRenderView;
		Pair<Block, Block> pair = (Pair<Block, Block>) view.getBlockEntityRenderAttachment(blockPos);
		Axis axis = blockState.get(FullSlabBlock.AXIS);
		BlockState positiveState = Helper.getState(pair.getFirst(), axis, true);
		BlockState negativeState = Helper.getState(pair.getSecond(), axis, false);
		BlockRenderManager manager = MinecraftClient.getInstance().getBlockRenderManager();
		renderContext.fallbackConsumer().accept(manager.getModel(positiveState));
		renderContext.fallbackConsumer().accept(manager.getModel(negativeState));
	}

	public void emitItemQuads(ItemStack itemStack, Supplier<Random> supplier, RenderContext renderContext) {

	}
}
