package dev.micalobia.full_slabs.mixin.item;

import dev.micalobia.full_slabs.FullSlabsMod;
import dev.micalobia.full_slabs.block.ExtraSlabBlock;
import dev.micalobia.full_slabs.block.entity.ExtraSlabBlockEntity;
import dev.micalobia.full_slabs.config.SlabExtra;
import dev.micalobia.full_slabs.util.MixinSelf;
import dev.micalobia.full_slabs.util.Utility;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin implements MixinSelf<BlockItem> {
	@Shadow
	public abstract Block getBlock();

	@Inject(at = @At("HEAD"), cancellable = true, method = "place(Lnet/minecraft/item/ItemPlacementContext;Lnet/minecraft/block/BlockState;)Z")
	private void skimOldBlocksWhenSlab(ItemPlacementContext context, BlockState state, CallbackInfoReturnable<Boolean> cir) {
		boolean isFull = state.isOf(FullSlabsMod.FULL_SLAB_BLOCK);
		boolean isExtra = state.isOf(FullSlabsMod.EXTRA_SLAB_BLOCK);
		if(!isFull && !isExtra) return;
		World world = context.getWorld();
		BlockPos pos = context.getBlockPos();
		BlockState activeState = world.getBlockState(pos);
		Block activeBlock = activeState.getBlock();
		ItemStack stack = context.getStack();
		BlockItem item = (BlockItem) stack.getItem();
		if(isFull) {
			Block placedBlock = ((BlockItem) stack.getItem()).getBlock();
			boolean activePositive = activeState.get(SlabBlock.TYPE) == SlabType.TOP;
			Utility.setFullSlabGhost(
					activePositive ? activeBlock : placedBlock,
					activePositive ? placedBlock : activeBlock
			);
		} else Utility.setExtraSlabGhost(activeBlock, item);
	}

	@Inject(method = "getPlacementState", at = @At("HEAD"), cancellable = true)
	private void interceptExtraSlabPlacement(ItemPlacementContext context, CallbackInfoReturnable<BlockState> cir) {
		if(!ExtraSlabBlockEntity.allowed(this.getBlock())) return;
		BlockState state = context.getWorld().getBlockState(context.getBlockPos());
		if(!(state.getBlock() instanceof SlabBlock)) return;
		if(!ExtraSlabBlockEntity.allowed(state, self())) return;
		Axis axis = state.get(Properties.AXIS);
		SlabType type = state.get(SlabBlock.TYPE);
		SlabExtra extra = ExtraSlabBlockEntity.get(this.getBlock());
		assert extra != null;
		boolean waterlogged = extra.waterloggable() && state.get(SlabBlock.WATERLOGGED);
		int light = Objects.requireNonNull(extra.getState(Utility.getDirection(type, axis))).getLuminance();
		BlockState ret = FullSlabsMod.EXTRA_SLAB_BLOCK.getDefaultState()
				.with(ExtraSlabBlock.AXIS, axis)
				.with(ExtraSlabBlock.TYPE, type)
				.with(ExtraSlabBlock.WATERLOGGED, waterlogged)
				.with(ExtraSlabBlock.LIGHT, light);
		assert ret != null;
		cir.setReturnValue(ret);
	}
}
