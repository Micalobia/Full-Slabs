package dev.micalobia.full_slabs.mixin.item;

import dev.micalobia.full_slabs.FullSlabsMod;
import dev.micalobia.full_slabs.block.ExtraSlabBlock;
import dev.micalobia.full_slabs.block.entity.ExtraSlabBlockEntity;
import dev.micalobia.full_slabs.block.entity.ExtraSlabBlockEntity.SlabExtra;
import dev.micalobia.full_slabs.util.MixinSelf;
import dev.micalobia.full_slabs.util.Utility;
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

import java.util.Objects;

@Mixin(WallStandingBlockItem.class)
public abstract class WallStandingBlockItemMixin extends BlockItem implements MixinSelf<WallStandingBlockItem> {

	public WallStandingBlockItemMixin(Block block, Settings settings) {
		super(block, settings);
	}

	@Inject(method = "getPlacementState", at = @At("HEAD"), cancellable = true)
	private void interceptExtraSlabPlacement(ItemPlacementContext context, CallbackInfoReturnable<BlockState> cir) {
		BlockState state = context.getWorld().getBlockState(context.getBlockPos());
		if(!(state.getBlock() instanceof SlabBlock)) return;
		if(!ExtraSlabBlockEntity.allowed(state, self())) return;
		Axis axis = state.get(Properties.AXIS);
		SlabType type = state.get(SlabBlock.TYPE);
		boolean waterlogged = state.get(SlabBlock.WATERLOGGED);
		SlabExtra extra = ExtraSlabBlockEntity.get(axis, self());
		assert extra != null;
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
