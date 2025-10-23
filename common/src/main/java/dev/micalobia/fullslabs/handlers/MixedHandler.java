package dev.micalobia.fullslabs.handlers;

import dev.micalobia.fullslabs.SlabRegistry;
import dev.micalobia.fullslabs.block.MixedSlabBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Nullable;

public interface MixedHandler {
    default MixedSlabBlock mixed() {
        return SlabRegistry.MIXED_SLAB.get();
    }

    default void randomTick(MixedContext.Sided context, ServerWorld world, BlockPos pos, Random random) {
    }

    default boolean emitsRedstonePower(MixedContext.Sided context) {
        return false;
    }

    default int getStrongRedstonePower(MixedContext.Sided context, BlockView world, BlockPos pos, Direction direction) {
        return 0;
    }

    default int getWeakRedstonePower(MixedContext.Sided context, BlockView world, BlockPos pos, Direction direction) {
        return 0;
    }

    default boolean hasComparatorOutput(MixedContext.Sided context) {
        return false;
    }

    default int getComparatorOutput(MixedContext.Sided context, World world, BlockPos pos, Direction direction) {
        return 0;
    }

    default void onProjectileHit(MixedContext.Sided context, World world, BlockHitResult hit, ProjectileEntity projectile) {
    }

    default void onSteppedOn(MixedContext.Sided context, World world, BlockPos pos, Entity entity) {
    }

    default void onLandedUpon(MixedContext.Sided context, World world, BlockPos pos, Entity entity, double fallDistance) {
        entity.handleFallDamage(fallDistance, 1.0F, entity.getDamageSources().fall());
    }

    default void onEntityLand(MixedContext.Sided context, BlockView world, Entity entity) {
        entity.setVelocity(entity.getVelocity().multiply(1d, 0d, 1d));
    }

    default void precipitationTick(MixedContext.Sided context, World world, BlockPos pos, Biome.Precipitation precipitation) {
    }

    default void onBlockBreakStart(MixedContext.Sided context, World world, BlockPos pos, PlayerEntity player) {
    }

    default void afterBreak(MixedContext.Sided context, World world, PlayerEntity player, BlockPos pos, @Nullable BlockEntity blockEntity, ItemStack tool) {
    }

    default void scheduledTick(MixedContext.Sided context, ServerWorld world, BlockPos pos, Random random) {
    }

    default void onStacksDropped(MixedContext.Sided context, ServerWorld world, BlockPos pos, ItemStack tool, boolean dropExperience) {
    }

    default ActionResult onUse(MixedContext.Sided context, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        return ActionResult.PASS;
    }

    default ActionResult onUseWithItem(MixedContext.Sided context, ItemStack stack, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return ActionResult.PASS;
    }
}
