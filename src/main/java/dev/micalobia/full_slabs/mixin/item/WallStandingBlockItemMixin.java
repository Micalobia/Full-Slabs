package dev.micalobia.full_slabs.mixin.item;

import dev.micalobia.full_slabs.FullSlabsMod;
import dev.micalobia.full_slabs.block.ExtraSlabBlock;
import dev.micalobia.full_slabs.block.entity.ExtraSlabBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.WallStandingBlockItem;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction.Axis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WallStandingBlockItem.class)
public abstract class WallStandingBlockItemMixin extends BlockItem {

	public WallStandingBlockItemMixin(Block block, Settings settings) {
		super(block, settings);
	}

	@Inject(method = "getPlacementState", at = @At("HEAD"), cancellable = true)
	private void interceptExtraSlabPlacement(ItemPlacementContext context, CallbackInfoReturnable<BlockState> cir) {
		if(!ExtraSlabBlockEntity.allowed(this.getBlock())) return;
		BlockState state = context.getWorld().getBlockState(context.getBlockPos());
		if(!(state.getBlock() instanceof SlabBlock)) return;
		Axis axis = state.get(Properties.AXIS);
		SlabType type = state.get(SlabBlock.TYPE);
		boolean waterlogged = state.get(SlabBlock.WATERLOGGED);
		BlockState ret = FullSlabsMod.EXTRA_SLAB_BLOCK.getDefaultState().with(ExtraSlabBlock.AXIS, axis).with(ExtraSlabBlock.TYPE, type).with(ExtraSlabBlock.WATERLOGGED, waterlogged);
		assert ret != null;
		cir.setReturnValue(ret);
	}
}
