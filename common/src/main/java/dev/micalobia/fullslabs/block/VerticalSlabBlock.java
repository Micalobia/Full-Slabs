package dev.micalobia.fullslabs.block;

import dev.micalobia.fullslabs.handlers.MixedHandlers;
import dev.micalobia.fullslabs.util.MixedType;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@MethodsReturnNonnullByDefault
public class VerticalSlabBlock extends Block implements SimpleWaterloggedBlock, SlabLike {
    private static final Map<SlabBlock, VerticalSlabBlock> MAP = new HashMap<>();
    public static final EnumProperty<Direction> DIRECTION = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<VerticalType> TYPE = EnumProperty.create("type", VerticalType.class);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final VoxelShape NORTH_SHAPE = Block.box(0f, 0f, 0f, 16f, 16f, 8f);
    public static final VoxelShape EAST_SHAPE = Block.box(8f, 0f, 0f, 16f, 16f, 16f);
    public static final VoxelShape SOUTH_SHAPE = Block.box(0f, 0f, 8f, 16f, 16f, 16f);
    public static final VoxelShape WEST_SHAPE = Block.box(0f, 0f, 0f, 8f, 16f, 16f);
    public static final Map<SlabBlock, VerticalSlabBlock> MAP_VIEW = Collections.unmodifiableMap(MAP);

    public final SlabBlock parent;

    public VerticalSlabBlock(SlabBlock block, Properties properties) {
        super(properties);
        this.parent = block;
        MAP.put(block, this);
        this.registerDefaultState(this.defaultBlockState().setValue(DIRECTION, Direction.WEST).setValue(TYPE, VerticalType.TOWARDS).setValue(WATERLOGGED, false));
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return state.getValue(TYPE) != VerticalType.FULL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DIRECTION, TYPE, WATERLOGGED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        var type = state.getValue(TYPE);
        if (type == VerticalType.FULL) return Shapes.block();
        var towards = type == VerticalType.TOWARDS;
        return switch (state.getValue(DIRECTION)) {
            case NORTH -> towards ? NORTH_SHAPE : SOUTH_SHAPE;
            case EAST -> towards ? EAST_SHAPE : WEST_SHAPE;
            case SOUTH -> towards ? SOUTH_SHAPE : NORTH_SHAPE;
            case WEST -> towards ? WEST_SHAPE : EAST_SHAPE;
            default -> throw new IllegalArgumentException();
        };
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        var stack = context.getItemInHand();
        var type = state.getValue(TYPE);
        if (type == VerticalType.FULL) return false;
        if (!(stack.getItem() instanceof BlockItem blockItem)) return false;
        var block = blockItem.getBlock();
        if (!(block instanceof SlabBlock)) return false;
        if (block != this.parent && !(MixedHandlers.hasHandler(block) && MixedHandlers.hasHandler(this))) return false;
        if (context.replacingClickedOnBlock())
            return isInside(state, context.getClickedPos(), context.getClickLocation());
        return true;
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public boolean placeLiquid(LevelAccessor world, BlockPos pos, BlockState state, FluidState fluidState) {
        return state.getValue(TYPE) != VerticalType.FULL && SimpleWaterloggedBlock.super.placeLiquid(world, pos, state, fluidState);
    }

    @Override
    public boolean canPlaceLiquid(@Nullable LivingEntity filler, BlockGetter world, BlockPos pos, BlockState state, Fluid fluid) {
        return state.getValue(TYPE) != VerticalType.FULL && SimpleWaterloggedBlock.super.canPlaceLiquid(filler, world, pos, state, fluid);
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            LevelReader world,
            ScheduledTickAccess tickView,
            BlockPos pos,
            Direction direction,
            BlockPos neighborPos,
            BlockState neighborState,
            RandomSource random
    ) {
        if (state.getValue(WATERLOGGED)) {
            tickView.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }

        return super.updateShape(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return type == PathComputationType.WATER && state.getFluidState().is(FluidTags.WATER);
    }

    @Override
    public Item asItem() {
        return this.parent.asItem();
    }

    // SlabLike impl

    @Override
    public BlockState getHalf(BlockState state, BlockGetter level, BlockPos pos, boolean isTowards) {
        var empty = state.getValue(WATERLOGGED) ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
        return switch (state.getValue(TYPE)) {
            case TOWARDS -> isTowards ? state : empty;
            case AWAY -> isTowards ? empty : state;
            case FULL -> state.setValue(TYPE, isTowards ? VerticalType.TOWARDS : VerticalType.AWAY);
        };
    }

    @Override
    public boolean isVanilla() {return false;}

    @Override
    public boolean isVertical() {return true;}

    @Override
    public boolean isMixed() {return false;}

    @Override
    public boolean supportsMixing() {return MixedHandlers.hasHandler(this);}

    @Override
    public boolean hasVertical() {return true;}

    @Override
    public boolean isDouble(BlockState state) {return state.getValue(TYPE) == VerticalType.FULL;}

    @Override
    public boolean isSingle(BlockState state) {return state.getValue(TYPE) != VerticalType.FULL;}

    @Override
    public boolean isTowards(BlockState state) {return state.getValue(TYPE) == VerticalType.TOWARDS;}

    @Override
    public MixedType getType(BlockState state) {
        return switch (state.getValue(DIRECTION)) {
            case UP, DOWN -> throw new AssertionError();
            case NORTH -> MixedType.NORTH;
            case SOUTH -> MixedType.SOUTH;
            case WEST -> MixedType.WEST;
            case EAST -> MixedType.EAST;
        };
    }

    @Override
    public Direction getDirection(BlockState state) {
        return switch (state.getValue(TYPE)) {
            case TOWARDS -> state.getValue(DIRECTION);
            case AWAY -> state.getValue(DIRECTION).getOpposite();
            case FULL -> throw new IllegalArgumentException("Not a half-slab!");
        };
    }

    @Override
    public SlabBlock getRoot() {
        return this.parent;
    }

    @Override
    public BlockState asDouble(BlockState state) {return state.setValue(TYPE, VerticalType.FULL).setValue(WATERLOGGED, false);}

    @Override
    public boolean isInside(BlockState state, BlockPos pos, Vec3 hit) {
        var type = state.getValue(TYPE);
        if (type == VerticalType.FULL) return false;
        var dir = state.getValue(DIRECTION);
        dir = type == VerticalType.TOWARDS ? dir : dir.getOpposite();
        return switch (dir) {
            case NORTH -> hit.z - pos.getZ() >= 0.5d;
            case SOUTH -> hit.z - pos.getZ() <= 0.5d;
            case WEST -> hit.x - pos.getX() >= 0.5d;
            case EAST -> hit.x - pos.getX() <= 0.5d;
            default -> false;
        };
    }

    // Static helpers and such

    public static @Nullable VerticalSlabBlock getVertical(SlabBlock slab) {return MAP.get(slab);}

    public static Optional<VerticalSlabBlock> tryGetVertical(Block block) {
        if (block instanceof SlabBlock slab)
            return Optional.ofNullable(MAP.get(slab));
        if (block instanceof VerticalSlabBlock slab) return Optional.of(slab);
        return Optional.empty();
    }

    // Safe to call if isSlabWithVertical returns true
    public static SlabBlock getRoot(Block block) {
        return tryGetRoot(block).orElseThrow(() -> new IllegalArgumentException("Not a slab or missing vertical!"));
    }

    public static Optional<SlabBlock> tryGetRoot(Block block) {
        if (block instanceof SlabBlock slab && hasVertical(slab)) return Optional.of(slab);
        if (block instanceof VerticalSlabBlock slab) return Optional.of(slab.parent);
        return Optional.empty();
    }

    public static boolean hasVertical(SlabBlock block) {
        return MAP.containsKey(block);
    }

    public enum VerticalType implements StringRepresentable {
        TOWARDS("towards"),
        AWAY("away"),
        FULL("full");

        private final String name;

        VerticalType(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
