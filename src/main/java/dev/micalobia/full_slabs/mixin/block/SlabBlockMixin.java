package dev.micalobia.full_slabs.mixin.block;

import dev.micalobia.full_slabs.FullSlabsMod;
import dev.micalobia.full_slabs.block.FullSlabBlock;
import dev.micalobia.full_slabs.block.entity.ExtraSlabBlockEntity;
import dev.micalobia.full_slabs.util.Utility;
import dev.micalobia.full_slabs.util.Utility.HitPart;
import net.minecraft.block.*;
import net.minecraft.block.enums.SlabType;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SlabBlock.class)
public abstract class SlabBlockMixin extends Block implements Waterloggable {
	@Shadow
	@Final
	public static EnumProperty<SlabType> TYPE;

	@Shadow
	@Final
	public static BooleanProperty WATERLOGGED;

	public SlabBlockMixin(Settings settings) {
		super(settings);
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		SlabType type = state.get(TYPE);
		if(type == SlabType.DOUBLE) return VoxelShapes.fullCube();
		return Utility.getShape(Utility.getDirection(type, state.get(Properties.AXIS)));
	}

	@Redirect(method = "canReplace", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z"))
	private boolean changeCanReplaceCondition(ItemStack stack, Item item, BlockState state, ItemPlacementContext context) {
		Item stackItem = stack.getItem();
		if(!(stackItem instanceof BlockItem blockItem)) return false;
		if(blockItem.getBlock() instanceof SlabBlock) return true;
		return ExtraSlabBlockEntity.allowed(state, blockItem);
	}

	@Inject(method = "canReplace", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemPlacementContext;getHitPos()Lnet/minecraft/util/math/Vec3d;"), cancellable = true)
	private void changeCanReplaceMath(BlockState state, ItemPlacementContext context, CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(Utility.insideSlab(state.getBlock(), context.getHitPos()));
	}

	@Inject(method = "getPlacementState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getFluidState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/fluid/FluidState;"), cancellable = true)
	private void changePlacementRules(ItemPlacementContext ctx, CallbackInfoReturnable<BlockState> cir) {
		BlockPos pos = ctx.getBlockPos();
		BlockState state = ctx.getWorld().getBlockState(pos);
		if(state.getBlock() instanceof SlabBlock) {
			Axis axis = state.get(Properties.AXIS);
			SlabType type = state.get(TYPE);
			int otherLight = state.getLuminance();
			int thisLight = getDefaultState().with(TYPE, type).with(Properties.AXIS, axis).getLuminance();
			BlockState ret = FullSlabsMod.FULL_SLAB_BLOCK.getDefaultState()
					.with(FullSlabBlock.AXIS, axis)
					.with(FullSlabBlock.LIGHT, Math.max(otherLight, thisLight));
			cir.setReturnValue(ret);
		} else {
			if(!Utility.getVerticalEnabled()) return;
			Direction hitSide = ctx.getSide();
			Direction facing = ctx.getPlayerFacing();
			HitPart part = Utility.getHitPart(hitSide, facing, pos, ctx.getHitPos());
			Direction slabDir = Utility.getTargetedDirection(hitSide, facing, pos, ctx.getHitPos());
			if(part == HitPart.CENTER) slabDir = slabDir.getOpposite();
			FluidState fluidState = ctx.getWorld().getFluidState(pos);
			cir.setReturnValue(getDefaultState()
					.with(TYPE, Utility.slabType(slabDir))
					.with(Properties.AXIS, slabDir.getAxis())
					.with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER));
		}
	}

	@Inject(method = "canReplace", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemPlacementContext;getHitPos()Lnet/minecraft/util/math/Vec3d;"), cancellable = true)
	private void changeReplacementRules(BlockState state, ItemPlacementContext context, CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(Utility.insideSlab(state.getBlock(), context.getHitPos()));
	}
}
