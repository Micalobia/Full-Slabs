package dev.micalobia.full_slabs.mixin.item;

import com.mojang.datafixers.util.Pair;
import dev.micalobia.full_slabs.FullSlabsMod;
import dev.micalobia.full_slabs.util.Utility;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {
	@Inject(at = @At("HEAD"), cancellable = true, method = "place(Lnet/minecraft/item/ItemPlacementContext;Lnet/minecraft/block/BlockState;)Z")
	private void skimOldBlocksWhenFullSlab(ItemPlacementContext context, BlockState state, CallbackInfoReturnable<Boolean> cir) {
		if(!state.isOf(FullSlabsMod.FULL_SLAB_BLOCK)) return;
		World world = context.getWorld();
		BlockPos pos = context.getBlockPos();
		BlockState activeState = world.getBlockState(pos);
		ItemStack stack = context.getStack();
		Block placedBlock = ((BlockItem) stack.getItem()).getBlock();
		Block activeBlock = activeState.getBlock();
		boolean activePositive = activeState.get(SlabBlock.TYPE) == SlabType.TOP;
		Utility.setGhostPair(
				Pair.of(
						activePositive ? activeBlock : placedBlock,
						activePositive ? placedBlock : activeBlock
				));
	}
}
