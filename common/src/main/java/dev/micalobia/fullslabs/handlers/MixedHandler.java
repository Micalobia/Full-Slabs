package dev.micalobia.fullslabs.handlers;

import dev.micalobia.fullslabs.SlabRegistry;
import dev.micalobia.fullslabs.block.MixedSlabBlock;
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
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public interface MixedHandler {
    default MixedSlabBlock mixed() {
        return SlabRegistry.MIXED_SLAB.get();
    }

    default void randomTick(MixedContext.Sided context, ServerLevel world, BlockPos pos, RandomSource random) {
    }

    default boolean isSignalSource(MixedContext.Sided context) {
        return false;
    }

    default int getDirectSignal(MixedContext.Sided context, BlockGetter world, BlockPos pos, Direction direction) {
        return 0;
    }

    default int getSignal(MixedContext.Sided context, BlockGetter world, BlockPos pos, Direction direction) {
        return 0;
    }

    default void onProjectileHit(MixedContext.Sided context, Level world, BlockHitResult hit, Projectile projectile) {
    }

    default void stepOn(MixedContext.Sided context, Level world, BlockPos pos, Entity entity) {
    }

    default void fallOn(MixedContext.Sided context, Level world, BlockPos pos, Entity entity, double fallDistance) {
        entity.causeFallDamage(fallDistance, 1.0F, entity.damageSources().fall());
    }

    default void updateEntityMovementAfterFallOn(MixedContext.Sided context, BlockGetter world, Entity entity) {
        entity.setDeltaMovement(entity.getDeltaMovement().multiply(1d, 0d, 1d));
    }

    default void handlePrecipitation(MixedContext.Sided context, Level world, BlockPos pos, Biome.Precipitation precipitation) {
    }

    default void attack(MixedContext.Sided context, Level world, BlockPos pos, Player player) {
    }

    default void playerDestroy(MixedContext.Sided context, Level world, Player player, BlockPos pos, @Nullable BlockEntity blockEntity, ItemStack tool) {
    }

    default void tick(MixedContext.Sided context, ServerLevel world, BlockPos pos, RandomSource random) {
    }

    default void spawnAfterBreak(MixedContext.Sided context, ServerLevel world, BlockPos pos, ItemStack tool, boolean dropExperience) {
    }

    default InteractionResult useWithoutItem(MixedContext.Sided context, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        return InteractionResult.PASS;
    }

    default InteractionResult useItemOn(MixedContext.Sided context, ItemStack stack, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return InteractionResult.PASS;
    }
}
