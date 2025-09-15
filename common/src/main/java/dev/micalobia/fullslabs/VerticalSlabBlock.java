package dev.micalobia.fullslabs;

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
    public static final EnumProperty<Axis> AXIS = Properties.HORIZONTAL_AXIS;
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
        this.setDefaultState(this.getDefaultState().with(AXIS, Axis.X).with(TYPE, VerticalType.NEGATIVE).with(WATERLOGGED, false));
    }


    @Override
    protected boolean hasSidedTransparency(BlockState state) {
        return state.get(TYPE) != VerticalType.DOUBLE;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AXIS, TYPE, WATERLOGGED);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        var axis = state.get(AXIS);
        return switch (state.get(TYPE)) {
            case POSITIVE -> axis == Axis.X ? EAST_SHAPE : SOUTH_SHAPE;
            case NEGATIVE -> axis == Axis.X ? WEST_SHAPE : NORTH_SHAPE;
            case DOUBLE -> VoxelShapes.fullCube();
        };
    }

    @Override
    protected boolean canReplace(BlockState state, ItemPlacementContext context) {
        var stack = context.getStack();
        if (!stack.isOf(this.parent.asItem())) return false;
        var type = state.get(TYPE);
        if (type == VerticalType.DOUBLE) return false;
        if (context.canReplaceExisting()) {
            var axis = state.get(AXIS);
            var face = context.getSide();
            var pos = context.getBlockPos();

            var local = axis == Axis.X ? context.getHitPos().x - pos.getX() : context.getHitPos().z - pos.getZ();
            var hitPositive = local >= 0.5d;
            var emptyIsPositive = type == VerticalType.NEGATIVE;
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
        return state.get(TYPE) != VerticalType.DOUBLE && Waterloggable.super.tryFillWithFluid(world, pos, state, fluidState);
    }

    @Override
    public boolean canFillWithFluid(@Nullable LivingEntity filler, BlockView world, BlockPos pos, BlockState state, Fluid fluid) {
        return state.get(TYPE) != VerticalType.DOUBLE && Waterloggable.super.canFillWithFluid(filler, world, pos, state, fluid);
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

    public record PurityResult(boolean pure, @Nullable String message) {
        public static PurityResult success() {
            return new PurityResult(true, null);
        }

        public static PurityResult fail(String msg) {
            return new PurityResult(false, msg);
        }
    }

    public static PurityResult isPure(SlabBlock block) {
        if (block instanceof BlockEntityProvider) return PurityResult.fail("Block has a block entity");
        var stateManager = block.getStateManager();
        var properties = stateManager.getProperties();
        if (properties.size() != 2) return PurityResult.fail("Unexpected property count");
        if (!properties.contains(SlabBlock.TYPE)) return PurityResult.fail("Missing `type` property");
        if (!properties.contains(Properties.WATERLOGGED)) return PurityResult.fail("Missing `waterlogged` property");
        var states = stateManager.getStates();
        if (states.size() != 6) return PurityResult.fail("Unexpected number of states");
        int luminance = -1;
        for (var state : states) {
            if (state.getRenderType() != BlockRenderType.MODEL) return PurityResult.fail("Non-model render type");
            if (state.hasRandomTicks()) return PurityResult.fail("Has random ticks");
            if (state.emitsRedstonePower()) return PurityResult.fail("Emits redstone power");
            ;
            if (state.hasComparatorOutput()) return PurityResult.fail("Has comparator output");
            int l = state.getLuminance();
            if (luminance < 0) luminance = l;
            else if (l != luminance) return PurityResult.fail("Inconsistent luminance across states");
        }
        return PurityResult.success();
    }

    public enum VerticalType implements StringIdentifiable {
        POSITIVE("positive"),
        NEGATIVE("negative"),
        DOUBLE("double");

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
