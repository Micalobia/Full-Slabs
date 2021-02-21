package dev.micalobia.full_slabs.mixin.item;

import dev.micalobia.full_slabs.block.Blocks;
import dev.micalobia.full_slabs.block.entity.FullSlabBlockEntity;
import dev.micalobia.full_slabs.util.Helper;
import dev.micalobia.full_slabs.util.LinkedSlabs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
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
	@Inject(method = "place(Lnet/minecraft/item/ItemPlacementContext;Lnet/minecraft/block/BlockState;)Z", at = @At("HEAD"), cancellable = true)
	public void placeMixedSlabsCorrectly(ItemPlacementContext context, BlockState state, CallbackInfoReturnable<Boolean> cir) {
		if(state.isOf(Blocks.FULL_SLAB_BLOCK)) {
			World world = context.getWorld();
			BlockPos pos = context.getBlockPos();
			BlockState currentState = world.getBlockState(pos);
			ItemStack stack = context.getStack();
			Block placedBlock = Helper.fetchBlock(stack.getItem());
			Block currentBlock = LinkedSlabs.horizontal(currentState.getBlock());
			boolean currentPositive = Helper.isPositive(currentState);
			FullSlabBlockEntity entity = new FullSlabBlockEntity(
					currentPositive ? currentBlock : placedBlock,
					currentPositive ? placedBlock : currentBlock
			);
			boolean bl = world.setBlockState(pos, state, 11);
			world.setBlockEntity(pos, entity);
			cir.setReturnValue(bl);
		}
	}
}
