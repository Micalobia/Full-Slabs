package dev.micalobia.full_slabs.mixin.client.render.block;

import dev.micalobia.full_slabs.block.Blocks;
import dev.micalobia.full_slabs.block.entity.FullSlabBlockEntity;
import dev.micalobia.full_slabs.util.Helper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(BlockRenderManager.class)
public class BlockRenderManagerMixin {
	@Shadow
	@Final
	private BlockModelRenderer blockModelRenderer;

	@Shadow
	@Final
	private BlockModels models;

	@Shadow
	@Final
	private Random random;

	@Inject(method = "renderDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/BlockModels;getModel(Lnet/minecraft/block/BlockState;)Lnet/minecraft/client/render/model/BakedModel;"), cancellable = true)
	public void renderSlabDamage(BlockState state, BlockPos pos, BlockRenderView world, MatrixStack matrix, VertexConsumer vertexConsumer, CallbackInfo ci) {
		if(Helper.isAnySlab(state.getBlock()) && Helper.isDoubleSlab(state)) {
			MinecraftClient client = MinecraftClient.getInstance();
			Vec3d hit = client.crosshairTarget.getPos();
			Axis axis = Helper.axisFromSlab(state);
			boolean positive = Helper.isPositive(hit, pos, axis);
			BlockState trueState;
			if(state.isOf(Blocks.FULL_SLAB_BLOCK)) {
				FullSlabBlockEntity entity = (FullSlabBlockEntity) world.getBlockEntity(pos);
				Block slab = positive ? entity.getPositiveSlab() : entity.getNegativeSlab();
				trueState = Helper.getState(slab, axis, positive);
			} else trueState = Helper.getState(state.getBlock(), axis, positive);
			BakedModel bakedModel = this.models.getModel(trueState);
			long l = trueState.getRenderingSeed(pos);
			this.blockModelRenderer.render(world, bakedModel, trueState, pos, matrix, vertexConsumer, true, this.random, l, OverlayTexture.DEFAULT_UV);
			ci.cancel();
		}
	}
}
