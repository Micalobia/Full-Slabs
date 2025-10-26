package dev.micalobia.fullslabs.block;

import com.google.common.collect.ImmutableList;
import dev.micalobia.fullslabs.SlabRegistry;
import dev.micalobia.fullslabs.block.VerticalSlabBlock.VerticalType;
import dev.micalobia.fullslabs.block.entity.MixedSlabBlockEntity;
import dev.micalobia.fullslabs.ducks.MixedSlabBlockDuck;
import dev.micalobia.fullslabs.handlers.MixedConsumer;
import dev.micalobia.fullslabs.handlers.MixedContext;
import dev.micalobia.fullslabs.handlers.MixedFunction;
import dev.micalobia.fullslabs.util.Utility;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiFunction;

public final class MixedSlabBlock extends Block implements BlockEntityProvider, MixedSlabBlockDuck {
    public static final EnumProperty<MixedType> TYPE = EnumProperty.of("type", MixedType.class);

    @ApiStatus.Internal
    @Nullable
    public static PlayerEntity cachedPlayer = null;

    public MixedSlabBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(TYPE);
    }

    @Override
    protected boolean hasRandomTicks(BlockState state) {
        return true; // Not ideal, disables the check that skips random ticks in chunk sections even when it isn't needed
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        forwardSides(world, pos, ctx -> ctx.handler().randomTick(ctx, world, pos, random));
    }

    @Override
    protected boolean emitsRedstonePower(BlockState state) {
        return true; // Not ideal, mixed into redstone dust to accurately connect
    }

    // This reflects the truth
    public boolean emitsRedstonePower(BlockView world, BlockPos pos) {
        return forwardSidesValue(world, pos, ctx -> ctx.handler().emitsRedstonePower(ctx), Boolean::logicalOr);
    }

    @Override
    protected int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return forwardSidesValue(world, pos, ctx -> ctx.handler().getWeakRedstonePower(ctx, world, pos, direction), Math::max);
    }

    @Override
    protected int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return forwardSidesValue(world, pos, ctx -> ctx.handler().getStrongRedstonePower(ctx, world, pos, direction), Math::max);
    }

    @Override
    protected void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
        forwardSide(world, hit.getBlockPos(), hit.getPos(), ctx -> ctx.handler().onProjectileHit(ctx, world, hit, projectile));
    }

    @Override
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
        forwardSide(world, pos, entity.getEntityPos(), ctx -> ctx.handler().onSteppedOn(ctx, world, pos, entity));
    }

    @Override
    public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
        forwardSide(world, pos, entity.getEntityPos(), ctx -> ctx.handler().onLandedUpon(ctx, world, pos, entity, fallDistance));
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onEntityLand(BlockView world, Entity entity) {
        // using getLandingPos instead of getStandingPos since that's where this method is called from in Entity
        forwardSide(world, entity.getLandingPos(), entity.getEntityPos(), ctx -> ctx.handler().onEntityLand(ctx, world, entity));
    }

    @Override
    public void precipitationTick(BlockState state, World world, BlockPos pos, Biome.Precipitation precipitation) {
        forwardSides(world, pos, ctx -> ctx.handler().precipitationTick(ctx, world, pos, precipitation));
    }

    @Override
    protected void onBlockBreakStart(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        forwardSides(world, pos, ctx -> ctx.handler().onBlockBreakStart(ctx, world, pos, player));
    }

    @Override
    public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        forwardSides(world, pos, ctx -> ctx.handler().afterBreak(ctx, world, player, pos, blockEntity, tool));
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        forwardSides(world, pos, ctx -> ctx.handler().scheduledTick(ctx, world, pos, random));
    }

    @Override
    protected void onStacksDropped(BlockState state, ServerWorld world, BlockPos pos, ItemStack tool, boolean dropExperience) {
        forwardSides(world, pos, ctx -> ctx.handler().onStacksDropped(ctx, world, pos, tool, dropExperience));
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        return forwardSideValue(world, pos, hit.getPos(), ctx -> ctx.handler().onUse(ctx, world, pos, player, hit));
    }

    @Override
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return forwardSideValue(world, pos, hit.getPos(), ctx -> ctx.handler().onUseWithItem(ctx, stack, world, pos, player, hand, hit));
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new MixedSlabBlockEntity(pos, state);
    }

    @Override
    protected ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
        var crosshair = Utility.crosshair(cachedPlayer, world.isClient());
        return forwardSideValue(world, pos, crosshair.getPos(), ctx -> new ItemStack(ctx.block()));
    }

    @Override
    protected float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        var hit = Utility.crosshair(player, ((World) world).isClient());
        return forwardSideValue(world, pos, hit.getPos(), ctx -> ctx.state().calcBlockBreakingDelta(player, world, pos));
    }

    @Override
    protected boolean onSyncedBlockEvent(BlockState state, World world, BlockPos pos, int type, int data) {
        if (type != 0) return false;
        world.updateListeners(pos, state, state, Block.NOTIFY_ALL_AND_REDRAW | Block.FORCE_STATE);
        return true;
    }

    public <T> T forward(BlockView world, BlockPos pos, MixedFunction<T, MixedContext.Sideless> function) {
        return function.apply(MixedContext.create(world, pos));
    }

    public <T> T forwardSideValue(BlockView world, BlockPos pos, boolean towards, MixedFunction<T, MixedContext.Sided> function) {
        return forward(world, pos, ctx -> function.apply(ctx.sided(towards)));
    }

    public <T> T forwardSideValue(BlockView world, BlockPos pos, Vec3d hit, MixedFunction<T, MixedContext.Sided> function) {
        return forward(world, pos, ctx -> {
            var type = ctx.mixedState().get(TYPE);
            var towards = type.isAxisTargetTowards(hit, pos);
            return function.apply(ctx.sided(towards));
        });
    }

    public void forwardSide(BlockView world, BlockPos pos, boolean towards, MixedConsumer<MixedContext.Sided> consumer) {
        // The field is to suppress a warning
        var ignored = this.<Void>forwardSideValue(world, pos, towards, ctx -> {
            consumer.apply(ctx);
            return null;
        });
    }

    public void forwardSide(BlockView world, BlockPos pos, Vec3d hit, MixedConsumer<MixedContext.Sided> consumer) {
        this.<Void>forwardSideValue(world, pos, hit, ctx -> {
            consumer.apply(ctx);
            return null;
        });
    }

    public <T, R> R forwardSidesValue(BlockView world, BlockPos pos, MixedFunction<T, MixedContext.Sided> function, BiFunction<T, T, R> selector) {
        return forward(world, pos, ctx -> {
            var towardsValue = function.apply(ctx.sided(true));
            var awayValue = function.apply(ctx.sided(false));
            return selector.apply(towardsValue, awayValue);
        });
    }

    public void forwardSides(BlockView world, BlockPos pos, MixedConsumer<MixedContext.Sided> consumer) {
        this.<Void, Void>forwardSidesValue(world, pos, ctx -> {
            consumer.apply(ctx);
            return null;
        }, (a, b) -> null);
    }

    public boolean towards(BlockState state, BlockHitResult hit) {
        return towards(state, hit.getPos(), hit.getBlockPos());
    }

    public boolean towards(BlockState state, Vec3d hit, BlockPos pos) {
        return state.get(TYPE).isAxisTargetTowards(hit, pos);
    }

    public enum MixedType implements StringIdentifiable {
        NORTH("north", Direction.NORTH),
        SOUTH("south", Direction.SOUTH),
        EAST("east", Direction.EAST),
        WEST("west", Direction.WEST),
        VERTICAL("vertical", Direction.UP);

        private static final List<MixedType> CARDINAL = ImmutableList.of(
                NORTH, SOUTH, EAST, WEST
        );
        public final Direction direction;
        private final String name;

        MixedType(String name, Direction direction) {
            this.name = name;
            this.direction = direction;
        }

        public static List<MixedType> cardinal() {
            return CARDINAL;
        }

        public static MixedType fromState(BlockState state) {
            var block = state.getBlock();
            if (block instanceof SlabBlock) return VERTICAL;
            if (block instanceof VerticalSlabBlock) return switch (state.get(Properties.HORIZONTAL_FACING)) {
                case UP, DOWN -> throw new AssertionError();
                case NORTH -> NORTH;
                case SOUTH -> SOUTH;
                case WEST -> WEST;
                case EAST -> EAST;
            };
            if (block == SlabRegistry.MIXED_SLAB.get()) return state.get(TYPE);
            throw new IllegalArgumentException("Not a slab!");
        }

        @Override
        public String asString() {
            return this.name;
        }

        public BlockState state(SlabBlock slab, boolean towards) {
            if (!VerticalSlabBlock.hasVertical(slab))
                throw new IllegalArgumentException("%s is missing a vertical".formatted(slab));
            if (this == VERTICAL)
                return slab.getDefaultState().with(Properties.SLAB_TYPE, towards ? SlabType.TOP : SlabType.BOTTOM);
            return VerticalSlabBlock.getVertical(slab).getDefaultState().with(VerticalSlabBlock.TYPE, towards ? VerticalType.TOWARDS : VerticalType.AWAY).with(Properties.HORIZONTAL_FACING, this.direction);
        }

        public boolean isAxisTargetTowards(Vec3d hit, BlockPos pos) {
            return switch (direction.getAxis()) {
                case X -> hit.x - pos.getX() > 0.5d ? Direction.EAST : Direction.WEST;
                case Y -> hit.y - pos.getY() > 0.5d ? Direction.UP : Direction.DOWN;
                case Z -> hit.z - pos.getZ() > 0.5d ? Direction.SOUTH : Direction.NORTH;
            } == direction;
        }

    }
}
