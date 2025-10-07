package dev.micalobia.fullslabs.handlers;

import dev.micalobia.fullslabs.SlabRegistry;
import dev.micalobia.fullslabs.block.MixedSlabBlock;
import dev.micalobia.fullslabs.block.entity.MixedSlabBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
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

    default void randomTick(Context context, BlockState state, ServerWorld world, BlockPos pos, Random random) {
    }

    default boolean emitsRedstonePower(Context context, BlockState state) {
        return false;
    }

    default int getStrongRedstonePower(Context context, BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return 0;
    }

    default int getWeakRedstonePower(Context context, BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return 0;
    }

    default boolean hasComparatorOutput(Context context, BlockState state) {
        return false;
    }

    default int getComparatorOutput(Context context, BlockState state, World world, BlockPos pos) {
        return 0;
    }

    default void onProjectileHit(Context context, World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
    }

    default void onSteppedOn(Context context, World world, BlockPos pos, BlockState state, Entity entity) {
    }

    default void onLandedUpon(Context context, World world, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
        entity.handleFallDamage(fallDistance, 1.0F, entity.getDamageSources().fall());
    }

    default void onEntityLand(Context context, BlockView world, Entity entity) {
        entity.setVelocity(entity.getVelocity().multiply(1d, 0d, 1d));
    }

    default void precipitationTick(Context context, BlockState state, World world, BlockPos pos, Biome.Precipitation precipitation) {
    }

    default void onBlockBreakStart(Context context, BlockState state, World world, BlockPos pos, PlayerEntity player) {
    }

    default void afterBreak(Context context, World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
    }

    default void scheduledTick(Context context, BlockState state, ServerWorld world, BlockPos pos, Random random) {
    }

    default void onStacksDropped(Context context, BlockState state, ServerWorld world, BlockPos pos, ItemStack tool, boolean dropExperience) {
    }

    default ActionResult onUse(Context context, BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        return ActionResult.PASS;
    }

    default ActionResult onUseWithItem(Context context, ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return ActionResult.PASS;
    }

    record Context(MixedSlabBlockEntity blockEntity, boolean towards) {
        public boolean replaceBlock(Block block) {
            return blockEntity.setBlock(block, towards);
        }
    }
}
