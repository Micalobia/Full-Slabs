package dev.micalobia.full_slabs.mixin.block;

import dev.micalobia.full_slabs.block.*;
import dev.micalobia.full_slabs.block.Blocks;
import dev.micalobia.full_slabs.block.enums.SlabState;
import dev.micalobia.full_slabs.util.Helper;
import dev.micalobia.full_slabs.util.LinkedSlabs;
import net.minecraft.block.*;
import net.minecraft.block.enums.SlabType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SlabBlock.class)
public abstract class SlabBlockMixin extends Block implements Waterloggable, ISlabBlock {

	@Shadow
	@Final
	public static EnumProperty<SlabType> TYPE;

	@Shadow
	@Final
	public static BooleanProperty WATERLOGGED;



	@Shadow @Final public static VoxelShape TOP_SHAPE;

	@Shadow @Final public static VoxelShape BOTTOM_SHAPE;

	private static final VoxelShape TOP_OUTLINE_SHAPE;
	private static final VoxelShape BOTTOM_OUTLINE_SHAPE;

	public SlabBlockMixin(AbstractBlock.Settings settings) {
		super(settings);
		this.setDefaultState(this.getDefaultState().with(TYPE, SlabType.BOTTOM).with(WATERLOGGED, false));
	}

	public boolean canReplace(BlockState state, ItemPlacementContext ctx) {
		if (!(Helper.fetchBlock(ctx.getStack().getItem()) instanceof SlabBlock)) return false;
		SlabType type = state.get(TYPE);
		if(type == SlabType.DOUBLE) return false;
		if(ctx.canReplaceExisting()) return isInside(state, ctx.getSide(), ctx.getHitPos(), ctx.getBlockPos());
		return true;
	}

	public BlockState getPlacementState(ItemPlacementContext ctx) {
		BlockPos pos = ctx.getBlockPos();
		World world = ctx.getWorld();
		BlockState state = world.getBlockState(pos);
		Block block = state.getBlock();

		Direction dir = ctx.getSide().getOpposite();
		Axis axis = dir.getAxis();

		if(state.isAir()) { // Just placed normally
			FluidState fluidState = world.getFluidState(pos);
			if(axis.isVertical()) {
				return getDefaultState()
						.with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER)
						.with(TYPE, dir.getDirection() == AxisDirection.POSITIVE ? SlabType.TOP : SlabType.BOTTOM);
			} else {
				return LinkedSlabs
						.vertical(this).getDefaultState()
						.with(VerticalSlabBlock.WATERLOGGED, fluidState.getFluid() == Fluids.WATER)
						.with(VerticalSlabBlock.STATE, SlabState.fromAxisDirection(dir.getDirection()))
						.with(VerticalSlabBlock.AXIS, axis);
			}
		} else {
			Block horizontalBlock = LinkedSlabs.horizontal(block);
			if(horizontalBlock.is(this)) { //This is the same slab type
				if(state.getBlock() instanceof SlabBlock)
					return state.with(TYPE, SlabType.DOUBLE).with(WATERLOGGED, false);
				else return state.with(VerticalSlabBlock.STATE, SlabState.DOUBLE).with(WATERLOGGED, false);
			} else { //This is a slab of a different type
				return Blocks.FULL_SLAB_BLOCK.getDefaultState().with(FullSlabBlock.AXIS, axis);
			}
		}
	}

	public boolean isInside(BlockState state, Vec3d hit, BlockPos pos) {
		SlabType type = state.get(TYPE);
		if(type == SlabType.DOUBLE) return false;
		boolean isPositive = hit.getY() - pos.getY() > 0.5;
		return isPositive == (type == SlabType.BOTTOM);
	}

	public @Nullable Direction direction(BlockState state) {
		SlabType type = state.get(TYPE);
		if(type == SlabType.DOUBLE) return null;
		return type == SlabType.TOP ? Direction.UP : Direction.DOWN;
	}

	public VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
		SlabType slabType = state.get(TYPE);
		switch(slabType) {
			case DOUBLE:
				return VoxelShapes.fullCube();
			case TOP:
				return TOP_OUTLINE_SHAPE;
			default:
				return BOTTOM_OUTLINE_SHAPE;
		}
	}

	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext ctx) {
		SlabType slabType = state.get(TYPE);
		switch(slabType) {
			case DOUBLE:
				HitResult hit = MinecraftClient.getInstance().crosshairTarget;
				if (hit == null || hit.getType() != Type.BLOCK) return VoxelShapes.fullCube();
				boolean positive = isInside(state.with(TYPE, SlabType.BOTTOM), hit.getPos(), pos);
				return positive ? TOP_OUTLINE_SHAPE : BOTTOM_OUTLINE_SHAPE;
			case TOP:
				return TOP_OUTLINE_SHAPE;
			default:
				return BOTTOM_OUTLINE_SHAPE;
		}
	}

	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		SlabType slabType = state.get(TYPE);
		switch(slabType) {
			case DOUBLE:
				return VoxelShapes.fullCube();
			case TOP:
				return TOP_SHAPE;
			default:
				return BOTTOM_SHAPE;
		}
	}

	public Axis axis(BlockState state) {
		return Axis.Y;
	}

	static {
		TOP_OUTLINE_SHAPE = SlabBlockConstants.TOP_OUTLINE_SHAPE;
		BOTTOM_OUTLINE_SHAPE = SlabBlockConstants.BOTTOM_OUTLINE_SHAPE;
	}
}
