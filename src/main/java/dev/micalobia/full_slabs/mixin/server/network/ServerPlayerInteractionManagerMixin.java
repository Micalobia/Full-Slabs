package dev.micalobia.full_slabs.mixin.server.network;

import dev.micalobia.full_slabs.block.Blocks;
import dev.micalobia.full_slabs.block.FullSlabBlock;
import dev.micalobia.full_slabs.block.VerticalSlabBlock;
import dev.micalobia.full_slabs.block.entity.FullSlabBlockEntity;
import dev.micalobia.full_slabs.block.enums.SlabState;
import dev.micalobia.full_slabs.util.Helper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public abstract class ServerPlayerInteractionManagerMixin {
	@Shadow
	public ServerWorld world;

	@Shadow
	public ServerPlayerEntity player;

	@Shadow
	public abstract boolean isCreative();

	@Inject(method = "tryBreakBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;onBreak(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/player/PlayerEntity;)V"), cancellable = true)
	public void tryBreakBlockInjection(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {

		BlockState state = this.world.getBlockState(pos);
		Block block = state.getBlock();
		if(block.is(Blocks.FULL_SLAB_BLOCK)) {
			FullSlabBlockEntity entity = (FullSlabBlockEntity) this.world.getBlockEntity(pos);
			HitResult hit = getCrosshair(this.player);
			if(hit.getType() != Type.BLOCK) {
				cir.setReturnValue(false);
			} else {
				Vec3d hitPos = hit.getPos();
				Axis axis = state.get(FullSlabBlock.AXIS);
				boolean positive = Helper.isPositive(hitPos, pos, axis);
				Block leftover = positive ? entity.getNegativeSlab() : entity.getPositiveSlab();
				Block broken = positive ? entity.getPositiveSlab() : entity.getNegativeSlab();
				BlockState leftoverState = Helper.getState(leftover, axis, !positive);
				BlockState brokenState = Helper.getState(broken, axis, positive);
				breakSlab(brokenState, leftoverState, pos);
				cir.setReturnValue(true);
			}
		} else if(block instanceof SlabBlock && state.get(SlabBlock.TYPE) == SlabType.DOUBLE) {
			HitResult hit = getCrosshair(this.player);
			if(hit.getType() != Type.BLOCK) {
				cir.setReturnValue(false);
			} else {
				Vec3d hitPos = hit.getPos();
				boolean positive = Helper.isPositive(hitPos, pos, Axis.Y);
				BlockState brokenState = state.with(SlabBlock.TYPE, positive ? SlabType.TOP : SlabType.BOTTOM);
				BlockState leftoverState = state.with(SlabBlock.TYPE, positive ? SlabType.BOTTOM : SlabType.TOP);
				breakSlab(brokenState, leftoverState, pos);
				cir.setReturnValue(true);
			}
		} else if(block instanceof VerticalSlabBlock && state.get(VerticalSlabBlock.STATE) == SlabState.DOUBLE) {
			HitResult hit = getCrosshair(this.player);
			if(hit.getType() != Type.BLOCK) {
				cir.setReturnValue(false);
			} else {
				Vec3d hitPos = hit.getPos();
				Axis axis = state.get(VerticalSlabBlock.AXIS);
				boolean positive = Helper.isPositive(hitPos, pos, axis);
				BlockState brokenState = Helper.getState(block, axis, positive);
				BlockState leftoverState = Helper.getState(block, axis, !positive);
				breakSlab(brokenState, leftoverState, pos);
				cir.setReturnValue(true);
			}
		}
	}

	private HitResult getCrosshair(ServerPlayerEntity player) {
		float distance = player.isCreative() ? 4.5f : 3f;
		return this.player.raycast(distance, 0f, false);
	}

	private void breakSlab(BlockState brokenState, BlockState leftoverState, BlockPos pos) {
		Block broken = brokenState.getBlock();
		broken.onBreak(this.world, pos, brokenState, this.player);
		boolean changedBlock = this.world.setBlockState(pos, leftoverState, 3);
		if(changedBlock) broken.onBroken(this.world, pos, brokenState);
		if(!this.isCreative()) {
			ItemStack hand = this.player.getMainHandStack();
			ItemStack handCopy = hand.copy();
			boolean effectiveTool = this.player.isUsingEffectiveTool(brokenState);
			hand.postMine(this.world, brokenState, pos, this.player);
			if(changedBlock && effectiveTool) {
				broken.afterBreak(this.world, this.player, pos, brokenState, null, handCopy);
			}
		}
	}
}
