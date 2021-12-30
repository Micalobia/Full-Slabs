package dev.micalobia.full_slabs.mixin.client.network;

import dev.micalobia.full_slabs.FullSlabsMod;
import dev.micalobia.full_slabs.block.ExtraSlabBlock;
import dev.micalobia.full_slabs.block.SlabBlockUtility;
import dev.micalobia.full_slabs.block.entity.ExtraSlabBlockEntity;
import dev.micalobia.full_slabs.block.entity.FullSlabBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.fluid.Fluids;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {
	@Shadow
	@Final
	private MinecraftClient client;

	private boolean breakSlab(BlockState brokenState, BlockState leftoverState, BlockPos pos) {
		Block broken = brokenState.getBlock();
		broken.onBreak(client.world, pos, brokenState, client.player);
		Objects.requireNonNull(client.world);
		boolean changed = client.world.setBlockState(pos, leftoverState, 11);
		if(changed) broken.onBroken(client.world, pos, brokenState);
		return changed;
	}

	@Inject(method = "breakBlock", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;onBreak(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/player/PlayerEntity;)V"))
	private void interceptSlabBreak(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		Objects.requireNonNull(client.world);
		BlockState state = client.world.getBlockState(pos);
		if(state.isOf(FullSlabsMod.FULL_SLAB_BLOCK)) {
			FullSlabBlockEntity entity = (FullSlabBlockEntity) client.world.getBlockEntity(pos);
			if(entity == null) cir.setReturnValue(false);
			else {
				Objects.requireNonNull(client.crosshairTarget);
				Vec3d hit = client.crosshairTarget.getPos();
				boolean ret = breakSlab(entity.getSlabState(hit), entity.getOppositeSlabState(hit), pos);
				cir.setReturnValue(ret);
			}
		} else if(state.isOf(FullSlabsMod.EXTRA_SLAB_BLOCK)) {
			ExtraSlabBlockEntity entity = (ExtraSlabBlockEntity) client.world.getBlockEntity(pos);
			if(entity == null) cir.setReturnValue(false);
			else {
				Objects.requireNonNull(client.crosshairTarget);
				Vec3d hit = client.crosshairTarget.getPos();
				Axis axis = state.get(ExtraSlabBlock.AXIS);
				SlabType type = state.get(ExtraSlabBlock.TYPE);
				boolean waterlogged = state.get(ExtraSlabBlock.WATERLOGGED);
				Direction slabDir = SlabBlockUtility.getDirection(type, axis);
				Direction hitDir = SlabBlockUtility.getDirection(axis, hit, pos, type);
				BlockState slabState = entity.getBaseState().with(SlabBlock.WATERLOGGED, waterlogged);
				BlockState extraState = entity.getExtraState();
				boolean ret = breakSlab(extraState, slabState, pos);
				if(hitDir == slabDir) {
					cir.setReturnValue(ret | breakSlab(slabState, waterlogged ? Fluids.WATER.getDefaultState().getBlockState() : Blocks.AIR.getDefaultState(), pos));
				} else {
					cir.setReturnValue(ret);
				}
			}
		} else if(state.getBlock() instanceof SlabBlock) {
			if(state.get(SlabBlock.TYPE) != SlabType.DOUBLE) return;
			Objects.requireNonNull(client.crosshairTarget);
			Vec3d hit = client.crosshairTarget.getPos();
			Axis axis = state.get(Properties.AXIS);
			boolean positive = SlabBlockUtility.isPositive(axis, hit, pos);
			BlockState brokenState = state.with(SlabBlock.TYPE, positive ? SlabType.TOP : SlabType.BOTTOM);
			BlockState leftoverState = state.with(SlabBlock.TYPE, positive ? SlabType.BOTTOM : SlabType.TOP);
			boolean ret = breakSlab(brokenState, leftoverState, pos);
			cir.setReturnValue(ret);
		}
	}
}
