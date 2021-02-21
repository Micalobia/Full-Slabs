package dev.micalobia.full_slabs.mixin.client.network;

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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.stat.StatHandler;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {
	@Shadow @Final private MinecraftClient client;

	@Shadow private ItemStack selectedStack;

	@Shadow public abstract ClientPlayerEntity createPlayer(ClientWorld world, StatHandler statHandler, ClientRecipeBook recipeBook);

	@Shadow private boolean breakingBlock;
	@Shadow private BlockPos currentBreakingPos;
	@Shadow private float currentBreakingProgress;
	protected Vec3d lastHit = Vec3d.ZERO;

	@Inject(method = "breakBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;onBreak(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/player/PlayerEntity;)V"), cancellable = true)
	public void breakSlabsCorrectly(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		ClientWorld world = this.client.world;
		BlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		if (block.is(Blocks.FULL_SLAB_BLOCK)) {
			FullSlabBlockEntity entity = (FullSlabBlockEntity) world.getBlockEntity(pos);
			Vec3d hit = this.client.crosshairTarget.getPos();
			Axis axis = state.get(FullSlabBlock.AXIS);
			boolean positive = Helper.isPositive(hit, pos, axis);
			Block leftover = positive ? entity.getNegativeSlab() : entity.getPositiveSlab();
			Block broken = positive ? entity.getPositiveSlab() : entity.getNegativeSlab();
			BlockState leftoverState = Helper.getState(leftover, axis, !positive);
			BlockState brokenState = Helper.getState(broken, axis, positive);
			cir.setReturnValue(breakSlab(brokenState, leftoverState, pos));
		} else if (block instanceof SlabBlock && state.get(SlabBlock.TYPE) == SlabType.DOUBLE) {
			Vec3d hit = this.client.crosshairTarget.getPos();
			boolean positive = Helper.isPositive(hit, pos, Axis.Y);
			BlockState leftoverState = state.with(SlabBlock.TYPE, positive ? SlabType.BOTTOM : SlabType.TOP);
			BlockState brokenState = state.with(SlabBlock.TYPE, positive ? SlabType.TOP : SlabType.BOTTOM);
			cir.setReturnValue(breakSlab(brokenState, leftoverState, pos));
		} else if (block instanceof VerticalSlabBlock && state.get(VerticalSlabBlock.STATE) == SlabState.DOUBLE) {
			Vec3d hit = this.client.crosshairTarget.getPos();
			Axis axis = state.get(VerticalSlabBlock.AXIS);
			boolean positive = Helper.isPositive(hit, pos, axis);
			BlockState leftoverState = Helper.getState(block, axis, !positive);
			BlockState brokenState = Helper.getState(block, axis, positive);
			cir.setReturnValue(breakSlab(brokenState, leftoverState, pos));
		}
	}

	private boolean breakSlab(BlockState brokenState, BlockState leftoverState, BlockPos pos) {
		Block broken = brokenState.getBlock();
		broken.onBreak(this.client.world, pos, brokenState, this.client.player);
		boolean changedBlock = this.client.world.setBlockState(pos, leftoverState, 11);
		if (changedBlock) broken.onBroken(this.client.world, pos, brokenState);
		return changedBlock;
	}

	@Inject(method = "updateBlockBreakingProgress", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;", ordinal = 1), cancellable = true)
	public void resetBreakingProgessIfSlabSwitch(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
		BlockState state = this.client.world.getBlockState(pos);
		Block block = state.getBlock();
		if (!Helper.isAnySlab(block) || !Helper.isDoubleSlab(state)) return;
		Vec3d currentHit = this.client.crosshairTarget.getPos();
		if (lastHit == Vec3d.ZERO) lastHit = currentHit;
		Axis axis = Helper.axisFromSlab(state);
		boolean isPositive = Helper.isPositive(currentHit, pos, axis);
		boolean wasPositive = Helper.isPositive(lastHit, pos, axis);
		if (isPositive != wasPositive) {
			this.currentBreakingProgress = 0f;
			cir.setReturnValue(false);
		}
		lastHit = currentHit;
	}
}
