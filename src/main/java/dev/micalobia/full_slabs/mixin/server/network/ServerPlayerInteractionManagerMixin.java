package dev.micalobia.full_slabs.mixin.server.network;

import dev.micalobia.full_slabs.FullSlabsMod;
import dev.micalobia.full_slabs.block.ExtraSlabBlock;
import dev.micalobia.full_slabs.block.SlabBlockUtility;
import dev.micalobia.full_slabs.block.entity.ExtraSlabBlockEntity;
import dev.micalobia.full_slabs.block.entity.FullSlabBlockEntity;
import dev.micalobia.full_slabs.util.Utility;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
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

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {
	@Shadow
	protected ServerWorld world;

	@Shadow
	@Final
	protected ServerPlayerEntity player;

	@Inject(method = "tryBreakBlock", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;onBreak(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/player/PlayerEntity;)V"))
	private void interceptSlabBreaking(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		BlockState state = world.getBlockState(pos);
		if(state.isOf(FullSlabsMod.FULL_SLAB_BLOCK)) {
			FullSlabBlockEntity entity = (FullSlabBlockEntity) world.getBlockEntity(pos);
			if(entity == null) cir.setReturnValue(false);
			else {
				HitResult hitResult = Utility.crosshair(player);
				if(hitResult.getType() != Type.BLOCK) cir.setReturnValue(false);
				else {
					Vec3d hit = hitResult.getPos();
					breakSlab(entity.getSlabState(hit), entity.getOppositeSlabState(hit), pos);
					cir.setReturnValue(true);
				}
			}
		} else if(state.isOf(FullSlabsMod.EXTRA_SLAB_BLOCK)) {
			ExtraSlabBlockEntity entity = (ExtraSlabBlockEntity) world.getBlockEntity(pos);
			if(entity == null) cir.setReturnValue(false);
			else {
				HitResult hitResult = Utility.crosshair(player);
				if(hitResult.getType() != Type.BLOCK) cir.setReturnValue(false);
				else {
					Vec3d hit = hitResult.getPos();
					Axis axis = state.get(ExtraSlabBlock.AXIS);
					SlabType type = state.get(ExtraSlabBlock.TYPE);
					boolean waterlogged = state.get(ExtraSlabBlock.WATERLOGGED);
					Direction slabDir = SlabBlockUtility.getDirection(type, axis);
					Direction hitDir = SlabBlockUtility.getDirection(axis, hit, pos, type);
					BlockState slabState = entity.getBaseState().with(SlabBlock.WATERLOGGED, waterlogged);
					BlockState extraState = entity.getExtraState();
					breakSlab(extraState, slabState, pos);
					if(hitDir == slabDir)
						breakSlab(slabState, waterlogged ? Fluids.WATER.getDefaultState().getBlockState() : Blocks.AIR.getDefaultState(), pos);
					cir.setReturnValue(true);
				}
			}
		} else if(state.getBlock() instanceof SlabBlock) {
			if(state.get(SlabBlock.TYPE) != SlabType.DOUBLE) return;
			HitResult hitResult = Utility.crosshair(player);
			if(hitResult.getType() != Type.BLOCK) cir.setReturnValue(false);
			else {
				Vec3d hit = hitResult.getPos();
				Axis axis = state.get(Properties.AXIS);
				boolean positive = SlabBlockUtility.isPositive(axis, hit, pos);
				BlockState brokenState = state.with(SlabBlock.TYPE, positive ? SlabType.TOP : SlabType.BOTTOM);
				BlockState leftoverState = state.with(SlabBlock.TYPE, positive ? SlabType.BOTTOM : SlabType.TOP);
				breakSlab(brokenState, leftoverState, pos);
				cir.setReturnValue(true);
			}
		}
	}

	private void breakSlab(BlockState brokenState, BlockState leftoverState, BlockPos pos) {
		Block broken = brokenState.getBlock();
		broken.onBreak(world, pos, brokenState, player);
		boolean changed = world.setBlockState(pos, leftoverState, 3);
		if(changed) broken.onBroken(world, pos, brokenState);
		if(!player.isCreative()) {
			ItemStack hand = player.getMainHandStack();
			ItemStack handCopy = hand.copy();
			boolean effectiveTool = player.canHarvest(brokenState);
			hand.postMine(world, brokenState, pos, player);
			if(changed && effectiveTool)
				broken.afterBreak(world, player, pos, brokenState, null, handCopy);
		}
	}
}
