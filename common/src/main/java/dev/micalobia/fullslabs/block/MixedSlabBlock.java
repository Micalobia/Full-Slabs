package dev.micalobia.fullslabs.block;

import com.google.common.collect.ImmutableList;
import dev.micalobia.fullslabs.SlabRegistry;
import dev.micalobia.fullslabs.block.VerticalSlabBlock.VerticalType;
import dev.micalobia.fullslabs.block.entity.MixedSlabBlockEntity;
import dev.micalobia.fullslabs.ducks.MixedSlabBlockDuck;
import dev.micalobia.fullslabs.handlers.MixedConsumer;
import dev.micalobia.fullslabs.handlers.MixedFunction;
import dev.micalobia.fullslabs.util.SlabContext;
import dev.micalobia.fullslabs.util.SlabContext.Side;
import dev.micalobia.fullslabs.util.Utility;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiFunction;

@MethodsReturnNonnullByDefault
public final class MixedSlabBlock extends Block implements EntityBlock, MixedSlabBlockDuck {
    public static final EnumProperty<MixedType> TYPE = EnumProperty.create("type", MixedType.class);

    @ApiStatus.Internal
    @Nullable
    public static Player cachedPlayer = null;

    public MixedSlabBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TYPE);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true; // Not ideal, disables the check that skips random ticks in chunk sections even when it isn't needed
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        forwardSides(world, pos, ctx -> ctx.mainHandler().randomTick(ctx, world, pos, random));
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true; // Not ideal, mixed into redstone dust to accurately connect
    }

    // This reflects the truth
    public boolean isSignalSource(BlockGetter world, BlockPos pos) {
        return forwardSidesValue(world, pos, ctx -> ctx.mainHandler().isSignalSource(ctx), Boolean::logicalOr);
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
        return forwardSidesValue(world, pos, ctx -> ctx.mainHandler().getSignal(ctx, world, pos, direction), Math::max);
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
        return forwardSidesValue(world, pos, ctx -> ctx.mainHandler().getDirectSignal(ctx, world, pos, direction), Math::max);
    }

    @Override
    protected void onProjectileHit(Level world, BlockState state, BlockHitResult hit, Projectile projectile) {
        forwardSide(world, hit.getBlockPos(), hit.getLocation(), ctx -> ctx.mainHandler().onProjectileHit(ctx, world, hit, projectile));
    }

    @Override
    public void stepOn(Level world, BlockPos pos, BlockState state, Entity entity) {
        forwardSide(world, pos, entity.position(), ctx -> ctx.mainHandler().stepOn(ctx, world, pos, entity));
    }

    @Override
    public void fallOn(Level world, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
        forwardSide(world, pos, entity.position(), ctx -> ctx.mainHandler().fallOn(ctx, world, pos, entity, fallDistance));
    }

    @SuppressWarnings("deprecation")
    @Override
    public void updateEntityMovementAfterFallOn(BlockGetter world, Entity entity) {
        // using getOnPosLegacy instead of getOnPos since that's where this method is called from in Entity
        forwardSide(world, entity.getOnPosLegacy(), entity.position(), ctx -> ctx.mainHandler().updateEntityMovementAfterFallOn(ctx, world, entity));
    }

    @Override
    public void handlePrecipitation(BlockState state, Level world, BlockPos pos, Biome.Precipitation precipitation) {
        forwardSides(world, pos, ctx -> ctx.mainHandler().handlePrecipitation(ctx, world, pos, precipitation));
    }

    @Override
    protected void attack(BlockState state, Level world, BlockPos pos, Player player) {
        forwardSides(world, pos, ctx -> ctx.mainHandler().attack(ctx, world, pos, player));
    }

    @Override
    public void playerDestroy(Level world, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        forwardSides(world, pos, ctx -> ctx.mainHandler().playerDestroy(ctx, world, player, pos, blockEntity, tool));
    }

    @Override
    protected void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        forwardSides(world, pos, ctx -> ctx.mainHandler().tick(ctx, world, pos, random));
    }

    @Override
    protected void spawnAfterBreak(BlockState state, ServerLevel world, BlockPos pos, ItemStack tool, boolean dropExperience) {
        forwardSides(world, pos, ctx -> ctx.mainHandler().spawnAfterBreak(ctx, world, pos, tool, dropExperience));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        return forwardSideValue(world, pos, hit.getLocation(), ctx -> ctx.mainHandler().useWithoutItem(ctx, world, pos, player, hit));
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return forwardSideValue(world, pos, hit.getLocation(), ctx -> ctx.mainHandler().useItemOn(ctx, stack, world, pos, player, hand, hit));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MixedSlabBlockEntity(pos, state);
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state, boolean includeData) {
        var crosshair = Utility.crosshair(cachedPlayer, world.isClientSide());
        return forwardSideValue(world, pos, crosshair.getLocation(), ctx -> new ItemStack(ctx.mainBlock()));
    }

    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter world, BlockPos pos) {
        var hit = Utility.crosshair(player, ((Level) world).isClientSide());
        return forwardSideValue(world, pos, hit.getLocation(), ctx -> ctx.mainState().getDestroyProgress(player, world, pos));
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level world, BlockPos pos, int type, int data) {
        if (type != 0) return false;
        world.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL_IMMEDIATE | Block.UPDATE_KNOWN_SHAPE);
        return true;
    }

    public <T> T forward(BlockGetter world, BlockPos pos, MixedFunction<T> function) {
        return forwardSideValue(world, pos, true, function);
    }

    public <T> T forwardSideValue(BlockGetter world, BlockPos pos, boolean towards, MixedFunction<T> function) {
        return function.apply(SlabContext.create(world, pos, Side.fromTowards(towards)));
    }

    public <T> T forwardSideValue(BlockGetter world, BlockPos pos, Vec3 hit, MixedFunction<T> function) {
        return forward(world, pos, ctx -> {
            var type = ctx.rootState().getValue(TYPE);
            var towards = type.isAxisTargetTowards(hit, pos);
            return forwardSideValue(world, pos, towards, function);
        });
    }

    public void forwardSide(BlockGetter world, BlockPos pos, boolean towards, MixedConsumer consumer) {
        this.<Void>forwardSideValue(world, pos, towards, ctx -> {
            consumer.apply(ctx);
            return null;
        });
    }

    public void forwardSide(BlockGetter world, BlockPos pos, Vec3 hit, MixedConsumer consumer) {
        this.<Void>forwardSideValue(world, pos, hit, ctx -> {
            consumer.apply(ctx);
            return null;
        });
    }

    public <T, R> R forwardSidesValue(BlockGetter world, BlockPos pos, MixedFunction<T> function, BiFunction<T, T, R> selector) {
        return forwardSideValue(world, pos, true, ctx -> {
            var towardsValue = function.apply(ctx);
            var awayValue = function.apply(ctx.flip());
            return selector.apply(towardsValue, awayValue);
        });
    }

    public void forwardSides(BlockGetter world, BlockPos pos, MixedConsumer consumer) {
        this.<Void, Void>forwardSidesValue(world, pos, ctx -> {
            consumer.apply(ctx);
            return null;
        }, (a, b) -> null);
    }

    public boolean towards(BlockState state, BlockHitResult hit) {
        return towards(state, hit.getLocation(), hit.getBlockPos());
    }

    public boolean towards(BlockState state, Vec3 hit, BlockPos pos) {
        return state.getValue(TYPE).isAxisTargetTowards(hit, pos);
    }

    public enum MixedType implements StringRepresentable {
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
            if (block instanceof VerticalSlabBlock)
                return switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
                    case UP, DOWN -> throw new AssertionError();
                    case NORTH -> NORTH;
                    case SOUTH -> SOUTH;
                    case WEST -> WEST;
                    case EAST -> EAST;
                };
            if (block == SlabRegistry.MIXED_SLAB) return state.getValue(TYPE);
            throw new IllegalArgumentException("Not a slab!");
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public BlockState state(SlabBlock slab, boolean towards) {
            if (!VerticalSlabBlock.hasVertical(slab))
                throw new IllegalArgumentException("%s is missing a vertical".formatted(slab));
            if (this == VERTICAL)
                return slab.defaultBlockState().setValue(BlockStateProperties.SLAB_TYPE, towards ? SlabType.TOP : SlabType.BOTTOM);
            return VerticalSlabBlock.getVertical(slab).defaultBlockState().setValue(VerticalSlabBlock.TYPE, towards ? VerticalType.TOWARDS : VerticalType.AWAY).setValue(BlockStateProperties.HORIZONTAL_FACING, this.direction);
        }

        public boolean isAxisTargetTowards(Vec3 pos, BlockPos location) {
            return switch (this.direction.getAxis()) {
                case X -> pos.x - location.getX() > 0.5d ? Direction.EAST : Direction.WEST;
                case Y -> pos.y - location.getY() > 0.5d ? Direction.UP : Direction.DOWN;
                case Z -> pos.z - location.getZ() > 0.5d ? Direction.SOUTH : Direction.NORTH;
            } == this.direction;
        }

    }
}
