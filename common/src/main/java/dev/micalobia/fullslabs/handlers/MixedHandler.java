package dev.micalobia.fullslabs.handlers;

import dev.micalobia.fullslabs.SlabRegistry;
import dev.micalobia.fullslabs.block.MixedSlabBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;


/**
 * The base class for mixed handlers. The BlockState passed in will be for the slab being acted on, if you want the parent mixed slab you can get it from the level.
 * Most of the signatures match what's in Block and BlockBehaviour, so you *could* make a block its own handler in some cases, although it's probably not recommended.
 */
public interface MixedHandler {
    default MixedSlabBlock mixed() {
        return SlabRegistry.MIXED_SLAB;
    }

    default void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
    }

    default boolean isSignalSource(BlockState state, BlockGetter level, BlockPos pos) {
        return false;
    }

    default int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 0;
    }

    default int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 0;
    }

    default void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
    }

    default void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
    }

    default void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
        entity.causeFallDamage(fallDistance, 1.0F, entity.damageSources().fall());
    }

    default void updateEntityMovementAfterFallOn(BlockState state, BlockGetter level, Entity entity) {
        entity.setDeltaMovement(entity.getDeltaMovement().multiply(1d, 0d, 1d));
    }

    default void handlePrecipitation(BlockState state, Level level, BlockPos pos, Biome.Precipitation precipitation) {
    }

    default void attack(BlockState state, Level level, BlockPos pos, Player player) {
    }

    default void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        player.awardStat(Stats.BLOCK_MINED.get(state.getBlock()));
        player.causeFoodExhaustion(0.005f);
        Block.dropResources(state, level, pos, blockEntity, player, tool);
    }

    default void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
    }

    default void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack tool, boolean dropExperience) {
    }

    default InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return InteractionResult.PASS;
    }

    default InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return InteractionResult.PASS;
    }
}
