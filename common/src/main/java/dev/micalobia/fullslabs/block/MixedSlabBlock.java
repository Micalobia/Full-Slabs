package dev.micalobia.fullslabs.block;

import dev.micalobia.fullslabs.FullSlabs;
import dev.micalobia.fullslabs.block.entity.MixedSlabBlockEntity;
import dev.micalobia.fullslabs.handlers.MixedHandlers;
import dev.micalobia.fullslabs.util.MixedType;
import dev.micalobia.fullslabs.util.Utility;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.function.ToIntFunction;

public final class MixedSlabBlock extends Block implements EntityBlock, SlabLike {
    public static final EnumProperty<MixedType> TYPE = EnumProperty.create("type", MixedType.class);
    public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL;
    public static final ToIntFunction<BlockState> LIGHT_EMISSION = state -> state.getValue(LEVEL);

    @ApiStatus.Internal
    @Nullable
    public static Player cachedPlayer = null;

    public MixedSlabBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(TYPE, MixedType.VERTICAL).setValue(LEVEL, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TYPE).add(LEVEL);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true; // Not ideal, disables the check that skips random ticks in chunk sections even when it isn't needed
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        getHalves(state, level, pos).accept(s -> MixedHandlers.getOrThrow(s).randomTick(s, level, pos, random));
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true; // Not ideal, mixed into redstone dust to accurately connect
    }

    // This reflects the truth
    public boolean isSignalSource(BlockState state, BlockGetter level, BlockPos pos) {
        return getHalves(state, level, pos).map(s -> MixedHandlers.getOrThrow(s).isSignalSource(s, level, pos)).merge(Boolean::logicalOr);
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return getHalves(state, level, pos).map(s -> MixedHandlers.getOrThrow(s).getSignal(s, level, pos, direction)).merge(Math::max);
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return getHalves(state, level, pos).map(s -> MixedHandlers.getOrThrow(s).getDirectSignal(s, level, pos, direction)).merge(Math::max);
    }

    @Override
    protected void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        var half = getHalf(state, level, hit.getBlockPos(), hit.getLocation());
        MixedHandlers.getOrThrow(half).onProjectileHit(level, half, hit, projectile);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        var half = getHalf(state, level, pos, entity.position());
        MixedHandlers.getOrThrow(half).stepOn(level, pos, half, entity);
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
        var half = getHalf(state, level, pos, entity.position());
        MixedHandlers.getOrThrow(half).fallOn(level, half, pos, entity, fallDistance);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void updateEntityMovementAfterFallOn(BlockGetter level, Entity entity) {
        // using getOnPosLegacy instead of getOnPos since that's where this method is called from in Entity
        var pos = entity.getOnPosLegacy();
        var state = level.getBlockState(entity.getOnPosLegacy());
        var half = getHalf(state, level, pos, entity.position());
        MixedHandlers.getOrThrow(half).updateEntityMovementAfterFallOn(half, level, entity);
    }

    @Override
    public void handlePrecipitation(BlockState state, Level level, BlockPos pos, Biome.Precipitation precipitation) {
        getHalves(state, level, pos).accept(s -> MixedHandlers.getOrThrow(s).handlePrecipitation(s, level, pos, precipitation));
    }

    @Override
    protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
        getHalves(state, level, pos).accept(s -> MixedHandlers.getOrThrow(s).attack(s, level, pos, player));
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        FullSlabs.LOGGER.warn("MixedSlabBlock playerDestroy called; Report to Full Slabs mod author");
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        getHalves(state, level, pos).accept(s -> MixedHandlers.getOrThrow(s).tick(s, level, pos, random));
    }

    @Override
    protected void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack tool, boolean dropExperience) {
        getHalves(state, level, pos).accept(s -> MixedHandlers.getOrThrow(s).spawnAfterBreak(s, level, pos, tool, dropExperience));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        var half = getHalf(state, level, pos, hit);
        return MixedHandlers.getOrThrow(half).useWithoutItem(half, level, pos, player, hit);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        var half = getHalf(state, level, pos, hit);
        return MixedHandlers.getOrThrow(half).useItemOn(stack, half, level, pos, player, hand, hit);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MixedSlabBlockEntity(pos, state);
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        var crosshair = Utility.crosshair(cachedPlayer, level.isClientSide());
        return new ItemStack(getHalf(state, level, pos, crosshair).getBlock());
    }

    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter world, BlockPos pos) {
        var hit = Utility.crosshair(player, ((Level) world).isClientSide());
        return getHalf(state, world, pos, isHitTowards(state, pos, hit)).getDestroyProgress(player, world, pos);
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level world, BlockPos pos, int type, int data) {
        if (type != 0) return false;
        world.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL_IMMEDIATE | Block.UPDATE_KNOWN_SHAPE);
        return true;
    }

    // SlabLike impl

    @Override
    public BlockState getHalf(BlockState state, BlockGetter level, BlockPos pos, boolean isTowards) {
        return level.getBlockEntity(pos) instanceof MixedSlabBlockEntity mixedEntity ? mixedEntity.getState(isTowards) : state.getValue(TYPE).state(FullSlabs.DEFAULT, isTowards);
    }

    @Override
    public boolean isVanilla() {return false;}

    @Override
    public boolean isVertical() {return false;}

    @Override
    public boolean isMixed() {return true;}

    @Override
    public boolean supportsMixing() {return false;}

    @Override
    public boolean hasVertical() {return false;}

    @Override
    public boolean isDouble(BlockState state) {return true;}

    @Override
    public boolean isSingle(BlockState state) {return false;}

    @Override
    public boolean isTowards(BlockState state) {return false;}

    @Override
    public MixedType getType(BlockState state) {return state.getValue(TYPE);}

    @Override
    public Direction getDirection(BlockState state) {throw new IllegalArgumentException("Not a half-slab!");}

    @Override
    public SlabBlock getRoot() {throw new RuntimeException("Cannot get the root slab of a mixed slab!");}

    @Override
    public BlockState asDouble(BlockState state) {return state;}

    @Override
    public boolean isInside(BlockState state, BlockPos pos, Vec3 hit) {return false;}
}
