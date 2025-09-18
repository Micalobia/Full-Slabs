package dev.micalobia.fullslabs;

import dev.micalobia.fullslabs.util.Result;
import net.minecraft.block.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

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
        //noinspection PointlessBooleanExpression
        if (true == true) return false;
        var stack = context.getStack();
        if (!stack.isOf(this.parent.asItem())) return false;
        if (state.get(TYPE) == VerticalType.FULL) return false;
        if (context.canReplaceExisting()) {
            var direction = state.get(DIRECTION);
            var axis = direction.getAxis();
            var face = context.getSide();
            var pos = context.getBlockPos();
            var local = axis == Axis.X ? context.getHitPos().x - pos.getX() : context.getHitPos().z - pos.getZ();
            var hitPositive = local >= 0.5d;
            var emptyIsPositive = direction == Direction.WEST || direction == Direction.NORTH;
            var towardEmpty = axis == Axis.X ? emptyIsPositive ? Direction.EAST : Direction.WEST : emptyIsPositive ? Direction.SOUTH : Direction.NORTH;
            if (face == towardEmpty) return true;
            if (face.getAxis() != axis && (hitPositive == emptyIsPositive)) return true;
            return false;
        }
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

    public static VerticalSlabBlock getVertical(SlabBlock block) {
        return MAP.get(block);
    }

    public static boolean hasVertical(SlabBlock block) {
        return MAP.containsKey(block);
    }

    public static Result isPure(SlabBlock block) {
        if (block instanceof BlockEntityProvider) return Result.fail("Block has a block entity");
        var stateManager = block.getStateManager();
        var properties = stateManager.getProperties();
        if (properties.size() != 2) return Result.fail("Unexpected property count");
        if (!properties.contains(SlabBlock.TYPE)) return Result.fail("Missing `type` property");
        if (!properties.contains(Properties.WATERLOGGED)) return Result.fail("Missing `waterlogged` property");
        var states = stateManager.getStates();
        if (states.size() != 6) return Result.fail("Unexpected number of states");
        int luminance = -1;
        for (var state : states) {
            if (state.getRenderType() != BlockRenderType.MODEL) return Result.fail("Non-model render type");
            if (state.hasRandomTicks()) return Result.fail("Has random ticks");
            if (state.emitsRedstonePower()) return Result.fail("Emits redstone power");
            if (state.hasComparatorOutput()) return Result.fail("Has comparator output");
            int l = state.getLuminance();
            if (luminance < 0) luminance = l;
            else if (l != luminance) return Result.fail("Inconsistent luminance across states");
        }
        return Result.success();
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
