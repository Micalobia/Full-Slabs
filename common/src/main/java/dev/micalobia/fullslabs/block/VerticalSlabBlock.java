package dev.micalobia.fullslabs.block;

import dev.micalobia.fullslabs.handlers.MixedHandlers;
import dev.micalobia.fullslabs.util.Utility;
import net.minecraft.block.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class VerticalSlabBlock extends Block implements Waterloggable {
    private static final Map<SlabBlock, VerticalSlabBlock> MAP = new HashMap<>();
    public static final EnumProperty<Direction> DIRECTION = Properties.HORIZONTAL_FACING;
    public static final EnumProperty<VerticalType> TYPE = EnumProperty.of("type", VerticalType.class);
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    public static final VoxelShape NORTH_SHAPE = Block.createCuboidShape(0f, 0f, 0f, 16f, 16f, 8f);
    public static final VoxelShape EAST_SHAPE = Block.createCuboidShape(8f, 0f, 0f, 16f, 16f, 16f);
    public static final VoxelShape SOUTH_SHAPE = Block.createCuboidShape(0f, 0f, 8f, 16f, 16f, 16f);
    public static final VoxelShape WEST_SHAPE = Block.createCuboidShape(0f, 0f, 0f, 8f, 16f, 16f);
    public static final Map<SlabBlock, VerticalSlabBlock> MAP_VIEW = Collections.unmodifiableMap(MAP);

    public final SlabBlock parent;

    public VerticalSlabBlock(SlabBlock block, Settings settings) {
        super(settings);
        this.parent = block;
        MAP.put(block, this);
        this.setDefaultState(this.getDefaultState().with(DIRECTION, Direction.WEST).with(TYPE, VerticalType.TOWARDS).with(WATERLOGGED, false));
    }

    @Override
    protected boolean hasSidedTransparency(BlockState state) {
        return state.get(TYPE) != VerticalType.FULL;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(DIRECTION, TYPE, WATERLOGGED);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        var type = state.get(TYPE);
        if (type == VerticalType.FULL) return VoxelShapes.fullCube();
        var towards = type == VerticalType.TOWARDS;
        return switch (state.get(DIRECTION)) {
            case NORTH -> towards ? NORTH_SHAPE : SOUTH_SHAPE;
            case EAST -> towards ? EAST_SHAPE : WEST_SHAPE;
            case SOUTH -> towards ? SOUTH_SHAPE : NORTH_SHAPE;
            case WEST -> towards ? WEST_SHAPE : EAST_SHAPE;
            default -> throw new IllegalArgumentException();
        };
    }

    @Override
    protected boolean canReplace(BlockState state, ItemPlacementContext context) {
        var stack = context.getStack();
        var type = state.get(TYPE);
        if (type == VerticalType.FULL) return false;
        var block = ((BlockItem) stack.getItem()).getBlock();
        if (!(block instanceof SlabBlock)) return false;
        if (block != this.parent && !(MixedHandlers.hasHandler(block) && MixedHandlers.hasHandler(this))) return false;
        if (context.canReplaceExisting())
            return Utility.isInsideSlab(state, context.getBlockPos(), context.getHitPos());
        return true;
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    public boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {
        return state.get(TYPE) != VerticalType.FULL && Waterloggable.super.tryFillWithFluid(world, pos, state, fluidState);
    }

    @Override
    public boolean canFillWithFluid(@Nullable LivingEntity filler, BlockView world, BlockPos pos, BlockState state, Fluid fluid) {
        return state.get(TYPE) != VerticalType.FULL && Waterloggable.super.canFillWithFluid(filler, world, pos, state, fluid);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(
            BlockState state,
            WorldView world,
            ScheduledTickView tickView,
            BlockPos pos,
            Direction direction,
            BlockPos neighborPos,
            BlockState neighborState,
            Random random
    ) {
        if (state.get(WATERLOGGED)) {
            tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }

        return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        return type == NavigationType.WATER && state.getFluidState().isIn(FluidTags.WATER);
    }

    @Override
    public Item asItem() {
        return parent.asItem();
    }

    // Static helpers and such

    public static VerticalSlabBlock getVertical(SlabBlock block) {
        return MAP.get(block);
    }

    public static SlabBlock getRoot(Block block) {
        if (block instanceof SlabBlock slab && hasVertical(slab)) return slab;
        if (block instanceof VerticalSlabBlock slab) return slab.parent;
        throw new IllegalArgumentException("Not a slab or missing vertical!");
    }

    public static boolean hasVertical(SlabBlock block) {
        return MAP.containsKey(block);
    }

    public enum VerticalType implements StringIdentifiable {
        TOWARDS("towards"),
        AWAY("away"),
        FULL("full");

        private final String name;

        VerticalType(String name) {
            this.name = name;
        }

        @Override
        public String asString() {
            return name;
        }
    }
}
