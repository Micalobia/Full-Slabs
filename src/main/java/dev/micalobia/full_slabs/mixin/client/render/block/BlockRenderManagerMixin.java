package dev.micalobia.full_slabs.mixin.client.render.block;

import com.mojang.datafixers.util.Pair;
import dev.micalobia.full_slabs.FullSlabsMod;
import dev.micalobia.full_slabs.util.Utility;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Environment(EnvType.CLIENT)
@Mixin(BlockRenderManager.class)
public class BlockRenderManagerMixin {
	@SuppressWarnings("unchecked")
	@Redirect(method = "renderDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/BlockModels;getModel(Lnet/minecraft/block/BlockState;)Lnet/minecraft/client/render/model/BakedModel;"))
	private BakedModel interceptSlabDamageRender(BlockModels blockModels, BlockState state_, BlockState state, BlockPos pos, BlockRenderView world, MatrixStack matrix, VertexConsumer vertexConsumer) {
		if(state.isOf(FullSlabsMod.FULL_SLAB_BLOCK)) {
			RenderAttachedBlockView view = (RenderAttachedBlockView) world;
			Axis axis = state.get(Properties.AXIS);
			assert MinecraftClient.getInstance().crosshairTarget != null;
			Vec3d hit = MinecraftClient.getInstance().crosshairTarget.getPos();
			Pair<Block, Block> pair = (Pair<Block, Block>) view.getBlockEntityRenderAttachment(pos);
			return blockModels.getModel(Utility.getSlabState(pair, axis, hit, pos));
		} else if(state.getBlock() instanceof SlabBlock && state.get(SlabBlock.TYPE) == SlabType.DOUBLE) {
			assert MinecraftClient.getInstance().crosshairTarget != null;
			Vec3d hit = MinecraftClient.getInstance().crosshairTarget.getPos();
			boolean positive = Utility.isPositive(state.get(Properties.AXIS), hit, pos);
			return blockModels.getModel(state.with(SlabBlock.TYPE, positive ? SlabType.TOP : SlabType.BOTTOM));
		} else return blockModels.getModel(state);
	}
}
