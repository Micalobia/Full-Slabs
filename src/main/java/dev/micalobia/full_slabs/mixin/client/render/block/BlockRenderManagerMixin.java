package dev.micalobia.full_slabs.mixin.client.render.block;

import dev.micalobia.full_slabs.FullSlabsMod;
import dev.micalobia.full_slabs.block.SlabBlockUtility;
import dev.micalobia.full_slabs.block.entity.ExtraSlabBlockEntity;
import dev.micalobia.full_slabs.block.entity.FullSlabBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(BlockRenderManager.class)
public class BlockRenderManagerMixin {
	private BlockRenderView FullSlabs$view;
	private BlockPos FullSlabs$pos;

	@Inject(method = "renderDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/BlockModels;getModel(Lnet/minecraft/block/BlockState;)Lnet/minecraft/client/render/model/BakedModel;"))
	private void skimSlabDamageInfo(BlockState state, BlockPos pos, BlockRenderView world, MatrixStack matrix, VertexConsumer vertexConsumer, CallbackInfo ci) {
		boolean needView = state.isOf(FullSlabsMod.FULL_SLAB_BLOCK) || state.isOf(FullSlabsMod.EXTRA_SLAB_BLOCK);
		if(needView || state.getBlock() instanceof SlabBlock) {
			this.FullSlabs$pos = pos;
			if(needView)
				this.FullSlabs$view = world;
		}
	}

	@ModifyArg(method = "renderDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/BlockModels;getModel(Lnet/minecraft/block/BlockState;)Lnet/minecraft/client/render/model/BakedModel;"), index = 0)
	private BlockState changeSlabDamageRender(BlockState state) {
		HitResult hitResult = MinecraftClient.getInstance().crosshairTarget;
		if(hitResult == null || hitResult.getType() != Type.BLOCK) return state;
		Vec3d hit = hitResult.getPos();
		if(state.isOf(FullSlabsMod.FULL_SLAB_BLOCK)) {
			FullSlabBlockEntity entity = (FullSlabBlockEntity) FullSlabs$view.getBlockEntity(FullSlabs$pos);
			if(entity == null) return state;
			return entity.getSlabState(hit);
		} else if(state.getBlock() instanceof SlabBlock && state.get(SlabBlock.TYPE) == SlabType.DOUBLE) {
			boolean positive = SlabBlockUtility.isPositive(state.get(Properties.AXIS), hit, this.FullSlabs$pos);
			return state.with(SlabBlock.TYPE, positive ? SlabType.TOP : SlabType.BOTTOM);
		} else if(state.isOf(FullSlabsMod.EXTRA_SLAB_BLOCK)) {
			ExtraSlabBlockEntity entity = (ExtraSlabBlockEntity) FullSlabs$view.getBlockEntity(FullSlabs$pos);
			if(entity == null) return state;
			return entity.getState(hit);
		}
		return state;
	}
}
