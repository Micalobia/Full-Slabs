package dev.micalobia.fullslabs.block;

import com.google.common.collect.ImmutableList;
import dev.micalobia.fullslabs.SlabRegistry;
import dev.micalobia.fullslabs.block.VerticalSlabBlock.VerticalType;
import dev.micalobia.fullslabs.block.entity.MixedSlabBlockEntity;
import dev.micalobia.fullslabs.handlers.MixedHandler.Context;
import dev.micalobia.fullslabs.handlers.MixedHandlers;
import dev.micalobia.fullslabs.util.Utility;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.SlabType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
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
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

public class MixedSlabBlock extends Block implements BlockEntityProvider {
    public static final EnumProperty<MixedType> TYPE = EnumProperty.of("type", MixedType.class);

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
        forward(world, pos, (context, slab, slabState) -> MixedHandlers.getOrThrow(slab).randomTick(context, slabState, world, pos, random));
    }

    @Override
    protected boolean emitsRedstonePower(BlockState state) {
        return true; // Not ideal, mixed into redstone dust to accurately connect
    }

    // This reflects the truth
    public boolean emitsRedstonePower(BlockView world, BlockPos pos) {
        return forwardValue(world, pos, ((context, slab, slabState) -> MixedHandlers.getOrThrow(slab).emitsRedstonePower(context, slabState)), Boolean::logicalOr);
    }

    @Override
    protected int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return forwardValue(world, pos, (context, slab, slabState) -> MixedHandlers.getOrThrow(slab).getWeakRedstonePower(context, slabState, world, pos, direction), Math::max);
    }

    @Override
    protected int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return forwardValue(world, pos, (context, slab, slabState) -> MixedHandlers.getOrThrow(slab).getStrongRedstonePower(context, slabState, world, pos, direction), Math::max);
    }

    @Override
    protected boolean hasComparatorOutput(BlockState state) {
        return true; // Not ideal, mixed into comparator block to prevent signal blocking
    }

    public boolean hasComparatorOutput(BlockView world, BlockPos pos) {
        return forwardValue(world, pos, ((context, slab, slabState) -> MixedHandlers.getOrThrow(slab).hasComparatorOutput(context, slabState)), Boolean::logicalOr);
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return forwardValue(world, pos, (context, slab, slabState) -> MixedHandlers.getOrThrow(slab).getComparatorOutput(context, slabState, world, pos), Math::max);
    }

    @Override
    protected void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
        var pos = hit.getBlockPos();
        forwardSide(world, pos, towards(state, hit.getPos(), pos), (context, slab, slabState) -> MixedHandlers.getOrThrow(slab).onProjectileHit(context, world, slabState, hit, projectile));
    }

    @Override
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
        forwardSide(world, pos, towards(state, entity.getPos(), pos), (context, slab, slabState) -> MixedHandlers.getOrThrow(slab).onSteppedOn(context, world, pos, slabState, entity));
    }

    @Override
    public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
        forwardSide(world, pos, towards(state, entity.getPos(), pos), (context, slab, slabState) -> MixedHandlers.getOrThrow(slab).onLandedUpon(context, world, slabState, pos, entity, fallDistance));
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onEntityLand(BlockView world, Entity entity) {
        // using getLandingPos instead of getStandingPos since that's where this method is called from in Entity
        var pos = entity.getLandingPos();
        var state = world.getBlockState(pos);
        forwardSide(world, pos, towards(state, entity.getPos(), pos), (context, slab, slabState) -> MixedHandlers.getOrThrow(slab).onEntityLand(context, world, entity));
    }

    @Override
    public void precipitationTick(BlockState state, World world, BlockPos pos, Biome.Precipitation precipitation) {
        forward(world, pos, (context, slab, slabState) -> MixedHandlers.getOrThrow(slab).precipitationTick(context, slabState, world, pos, precipitation));
    }

    @Override
    protected void onBlockBreakStart(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        forward(world, pos, (context, slab, slabState) -> MixedHandlers.getOrThrow(slab).onBlockBreakStart(context, slabState, world, pos, player));
    }

    @Override
    public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        forward(world, pos, (context, slab, slabState) -> MixedHandlers.getOrThrow(slab).afterBreak(context, world, player, pos, slabState, blockEntity, tool));
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        forward(world, pos, (context, slab, slabState) -> MixedHandlers.getOrThrow(slab).scheduledTick(context, slabState, world, pos, random));
    }

    @Override
    protected void onStacksDropped(BlockState state, ServerWorld world, BlockPos pos, ItemStack tool, boolean dropExperience) {
        forward(world, pos, (context, slab, slabState) -> MixedHandlers.getOrThrow(slab).onStacksDropped(context, slabState, world, pos, tool, dropExperience));
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        return forwardSideValue(world, pos, towards(state, hit.getPos(), pos), (context, slab, slabState) -> MixedHandlers.getOrThrow(slab).onUse(context, slabState, world, pos, player, hit));
    }

    @Override
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return forwardSideValue(world, pos, towards(state, hit.getPos(), pos), (context, slab, slabState) -> MixedHandlers.getOrThrow(slab).onUseWithItem(context, stack, slabState, world, pos, player, hand, hit));
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new MixedSlabBlockEntity(pos, state);
    }

    @Override
    protected ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
        return act(world, pos, mixed -> {
            var client = MinecraftClient.getInstance();
            var crosshair = Objects.requireNonNull((BlockHitResult) client.crosshairTarget);
            var picked = mixed.getTargetedSlab(crosshair);
            return new ItemStack(picked.asItem());
        });
    }

    @Override
    protected float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        HitResult hit;
        if (world instanceof ClientWorld) hit = Objects.requireNonNull(MinecraftClient.getInstance().crosshairTarget);
        else hit = Utility.crosshair(player);
        if (!(hit instanceof BlockHitResult bhr)) return 0f;
        return act(world, pos, mixed -> {
            var targeted = mixed.getTargetedState(bhr);
            return targeted.calcBlockBreakingDelta(player, world, pos);
        });
    }

    @Override
    protected boolean onSyncedBlockEvent(BlockState state, World world, BlockPos pos, int type, int data) {
        world.updateListeners(pos, state, state, Block.REDRAW_ON_MAIN_THREAD);
        return true;
    }

    private void forward(BlockView world, BlockPos pos, ForwardConsumer consumer) {
        this.<Void>forwardValue(world, pos, (context, block, state) -> {
            consumer.apply(context, block, state);
            return null;
        }, (a, b) -> null);
    }

    private <T> T forwardValue(BlockView world, BlockPos pos, ForwardFunction<T> function, BiFunction<T, T, T> selector) {
        return act(world, pos, mixed -> {
            var towardsState = mixed.getTowardsState();
            var towardsValue = function.apply(new Context(mixed, true), towardsState.getBlock(), towardsState);
            var awayState = mixed.getAwayState();
            var awayValue = function.apply(new Context(mixed, false), awayState.getBlock(), awayState);
            return selector.apply(towardsValue, awayValue);
        });
    }

    private void forwardSide(BlockView world, BlockPos pos, boolean towards, ForwardConsumer consumer) {
        this.<Void>forwardSideValue(world, pos, towards, (context, block, state) -> {
            consumer.apply(context, block, state);
            return null;
        });
    }

    private <T> T forwardSideValue(BlockView world, BlockPos pos, boolean towards, ForwardFunction<T> function) {
        return act(world, pos, mixed -> {
            var state = towards ? mixed.getTowardsState() : mixed.getAwayState();
            return function.apply(new Context(mixed, towards), state.getBlock(), state);
        });
    }

    public void act(BlockView world, BlockPos pos, MixedConsumer consumer) {
        this.<Void>act(world, pos, m -> {
            consumer.apply(m);
            return null;
        });
    }

    public <T> T act(BlockView world, BlockPos pos, MixedFunction<T> function) {
        var entity = world.getBlockEntity(pos);
        if (!(entity instanceof MixedSlabBlockEntity mixed)) throw new IllegalStateException("Not a Mixed Slab!");
        return function.apply(mixed);
    }

    public boolean towards(BlockState state, Vec3d hit, BlockPos pos) {
        return state.get(TYPE).isAxisTargetTowards(hit, pos);
    }

    @FunctionalInterface
    private interface ForwardFunction<T> {
        T apply(Context context, Block slab, BlockState slabState);
    }

    @FunctionalInterface
    private interface ForwardConsumer {
        void apply(Context context, Block slab, BlockState slabState);
    }

    @FunctionalInterface
    public interface MixedFunction<T> {
        T apply(MixedSlabBlockEntity mixedEntity);
    }

    @FunctionalInterface
    public interface MixedConsumer {
        void apply(MixedSlabBlockEntity mixedEntity);
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
            if (!VerticalSlabBlock.hasVertical(slab)) throw new IllegalArgumentException("slab");
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
