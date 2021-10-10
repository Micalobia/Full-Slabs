package dev.micalobia.full_slabs.mixin.item;

import dev.micalobia.full_slabs.FullSlabsMod;
import dev.micalobia.full_slabs.block.ExtraSlabBlock;
import dev.micalobia.full_slabs.block.entity.ExtraSlabBlockEntity;
import dev.micalobia.full_slabs.util.MixinSelf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.Waterloggable;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.WallStandingBlockItem;
import net.minecraft.state.property.Properties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(WallStandingBlockItem.class)
public abstract class WallStandingBlockItemMixin extends BlockItem implements MixinSelf<WallStandingBlockItem> {

	public WallStandingBlockItemMixin(Block block, Settings settings) {
		super(block, settings);
	}

	@Inject(method = "getPlacementState", at = @At("HEAD"), cancellable = true)
	private void interceptExtraSlabPlacement(ItemPlacementContext context, CallbackInfoReturnable<BlockState> cir) {
		BlockState state = context.getWorld().getBlockState(context.getBlockPos());
		Optional<BlockState> result = ExtraSlabBlockEntity.getExtra(state, self());
		if(result.isEmpty()) return;
		BlockState extra = result.get();
		boolean waterlogged = extra instanceof Waterloggable && state.get(SlabBlock.WATERLOGGED);
		int light = Math.max(extra.getLuminance(), state.getLuminance());
		BlockState ret = FullSlabsMod.EXTRA_SLAB_BLOCK.getDefaultState()
				.with(ExtraSlabBlock.AXIS, state.get(Properties.AXIS))
				.with(ExtraSlabBlock.TYPE, state.get(SlabBlock.TYPE))
				.with(ExtraSlabBlock.WATERLOGGED, waterlogged)
				.with(ExtraSlabBlock.LIGHT, light);
		cir.setReturnValue(ret);
	}
}
