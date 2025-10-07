package dev.micalobia.fullslabs.handlers;

import dev.micalobia.fullslabs.SlabRegistry;
import dev.micalobia.fullslabs.block.MixedSlabBlock;
import dev.micalobia.fullslabs.block.entity.MixedSlabBlockEntity;
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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

public interface MixedHandler {
    default MixedSlabBlock mixed() {
        return SlabRegistry.MIXED_SLAB.get();
    }

    Requirements requirements();

    default void randomTick(Context context, BlockState state, ServerWorld world, BlockPos pos, Random random) {
        throwWhenRequired("random ticks", this::requiresRandomTicks);
    }

    default int getStrongRedstonePower(Context context, BlockState state, BlockView world, BlockPos pos, Direction direction) {
        throwWhenRequired("strong redstone power", this::requiresRedstonePower);
        return 0;
    }

    default int getWeakRedstonePower(Context context, BlockState state, BlockView world, BlockPos pos, Direction direction) {
        throwWhenRequired("weak redstone power", this::requiresRedstonePower);
        return 0;
    }

    default int getComparatorOutput(Context context, BlockState state, World world, BlockPos pos) {
        throwWhenRequired("comparator output", this::requiresComparatorOutput);
        return 0;
    }

    default void onProjectileHit(Context context, World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
        throwWhenRequired("projectile hit", this::requiresProjectileHit);
    }

    default void onSteppedOn(Context context, World world, BlockPos pos, BlockState state, Entity entity) {
        throwWhenRequired("stepped on", this::requiresSteppedOn);
    }

    default void onLandedUpon(Context context, World world, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
        throwWhenRequired("landed upon", this::requiresLanding);
        entity.handleFallDamage(fallDistance, 1.0F, entity.getDamageSources().fall());
    }

    default void onEntityLand(Context context, BlockView world, Entity entity) {
        throwWhenRequired("entity land", this::requiresLanding);
        entity.setVelocity(entity.getVelocity().multiply(1d, 0d, 1d));
    }

    default void precipitationTick(Context context, BlockState state, World world, BlockPos pos, Biome.Precipitation precipitation) {
        throwWhenRequired("precipitation tick", this::requiresPrecipitationTicks);
    }

    default void onBlockBreakStart(Context context, BlockState state, World world, BlockPos pos, PlayerEntity player) {
        throwWhenRequired("on break start", this::requiresBlockBreaking);
    }

    default void afterBreak(Context context, World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        throwWhenRequired("after break", this::requiresBlockBreaking);
    }

    default void scheduledTick(Context context, BlockState state, ServerWorld world, BlockPos pos, Random random) {
        throwWhenRequired("scheduled tick", this::requiresScheduledTicks);
    }

    default void onStacksDropped(Context context, BlockState state, ServerWorld world, BlockPos pos, ItemStack tool, boolean dropExperience) {
        throwWhenRequired("stacks dropped", this::requiresStacksDropped);
    }

    default ActionResult onUse(Context context, BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        throwWhenRequired("on use", this::requiresOnUse);
        return ActionResult.PASS;
    }

    default ActionResult onUseWithItem(Context context, ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        throwWhenRequired("on use with item", this::requiresOnUse);
        return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
    }

    default boolean requiresRandomTicks() {
        return requirements().randomTicks;
    }

    default boolean requiresRedstonePower() {
        return requirements().redstonePower;
    }

    default boolean requiresComparatorOutput() {
        return requirements().comparatorOutput;
    }

    default boolean requiresProjectileHit() {
        return requirements().projectileHit;
    }

    default boolean requiresSteppedOn() {
        return requirements().steppedOn;
    }

    default boolean requiresLanding() {
        return requirements().landing;
    }

    default boolean requiresPrecipitationTicks() {
        return requirements().precipitationTicks;
    }

    default boolean requiresBlockBreaking() {
        return requirements().blockBreaking;
    }

    default boolean requiresScheduledTicks() {
        return requirements().scheduledTicks;
    }

    default boolean requiresStacksDropped() {
        return requirements().stacksDropped;
    }

    default boolean requiresOnUse() {
        return requirements().onUse;
    }

    private static void throwWhenRequired(String message, BooleanSupplier supplier) {
        if (supplier.getAsBoolean())
            throw new IllegalStateException("Trait requires %s!".formatted(message));
    }

    record Context(MixedSlabBlockEntity blockEntity, boolean towards) {
    }

    // This seems overkill and not terribly useful
    record Requirements(
            boolean randomTicks,
            boolean redstonePower,
            boolean comparatorOutput,
            boolean projectileHit,
            boolean steppedOn,
            boolean landing,
            boolean precipitationTicks,
            boolean blockBreaking,
            boolean scheduledTicks,
            boolean stacksDropped,
            boolean onUse
    ) {
        public static final Requirements EMPTY = new Requirements(false, false, false, false, false, false, false, false, false, false, false);

        public static Requirements.Builder builder() {
            return new Requirements.Builder();
        }

        public Requirements add(Requirements other) {
            return new Requirements(
                    this.randomTicks || other.randomTicks,
                    this.redstonePower || other.redstonePower,
                    this.comparatorOutput || other.comparatorOutput,
                    this.projectileHit || other.projectileHit,
                    this.steppedOn || other.steppedOn,
                    this.landing || other.landing,
                    this.precipitationTicks || other.precipitationTicks,
                    this.blockBreaking || other.blockBreaking,
                    this.scheduledTicks || other.scheduledTicks,
                    this.stacksDropped || other.stacksDropped,
                    this.onUse || other.onUse
            );
        }

        public static final class Builder {
            private boolean randomTicks = false;
            private boolean redstonePower = false;
            private boolean comparatorOutput = false;
            private boolean projectileHit = false;
            private boolean steppedOn = false;
            private boolean landing = false;
            private boolean precipitationTicks = false;
            private boolean blockBreaking = false;
            private boolean scheduledTicks = false;
            private boolean stacksDropped = false;
            private boolean onUse = false;

            private Builder() {}

            @Contract("_ -> this")
            public Requirements.Builder withRandomTicks(boolean value) {
                this.randomTicks = value;
                return this;
            }

            @Contract(" -> this")
            public Requirements.Builder withRandomTicks() {
                return withRandomTicks(true);
            }

            @Contract("_ -> this")
            public Requirements.Builder withRedstonePower(boolean value) {
                this.redstonePower = value;
                return this;
            }

            @Contract(" -> this")
            public Requirements.Builder withRedstonePower() {
                return withRedstonePower(true);
            }

            @Contract("_ -> this")
            public Requirements.Builder withComparatorOutput(boolean value) {
                this.comparatorOutput = value;
                return this;
            }

            @Contract(" -> this")
            public Requirements.Builder withComparatorOutput() {
                return withComparatorOutput(true);
            }

            @Contract("_ -> this")
            public Requirements.Builder withProjectileHit(boolean value) {
                this.projectileHit = value;
                return this;
            }

            @Contract(" -> this")
            public Requirements.Builder withProjectileHit() {
                return withProjectileHit(true);
            }

            @Contract("_ -> this")
            public Requirements.Builder withSteppedOn(boolean value) {
                this.steppedOn = value;
                return this;
            }

            @Contract(" -> this")
            public Requirements.Builder withSteppedOn() {
                return withSteppedOn(true);
            }

            @Contract("_ -> this")
            public Requirements.Builder withLanding(boolean value) {
                this.landing = value;
                return this;
            }

            @Contract(" -> this")
            public Requirements.Builder withLanding() {
                return withLanding(true);
            }

            @Contract("_ -> this")
            public Requirements.Builder withPrecipitationTicks(boolean value) {
                this.precipitationTicks = value;
                return this;
            }

            @Contract(" -> this")
            public Requirements.Builder withPrecipitationTicks() {
                return withPrecipitationTicks(true);
            }

            @Contract("_ -> this")
            public Requirements.Builder withBlockBreaking(boolean value) {
                this.blockBreaking = value;
                return this;
            }

            @Contract(" -> this")
            public Requirements.Builder withBlockBreaking() {
                return withBlockBreaking(true);
            }

            @Contract("_ -> this")
            public Requirements.Builder withScheduledTicks(boolean value) {
                this.scheduledTicks = value;
                return this;
            }

            @Contract(" -> this")
            public Requirements.Builder withScheduledTicks() {
                return withScheduledTicks(true);
            }

            @Contract("_ -> this")
            public Requirements.Builder withStacksDropped(boolean value) {
                this.stacksDropped = value;
                return this;
            }

            @Contract(" -> this")
            public Requirements.Builder withStacksDropped() {
                return withStacksDropped(true);
            }

            @Contract("_ -> this")
            public Requirements.Builder withOnUse(boolean value) {
                this.onUse = value;
                return this;
            }

            @Contract(" -> this")
            public Requirements.Builder withOnUse() {
                return withOnUse(true);
            }

            @Contract(" -> new")
            public Requirements build() {
                return new Requirements(
                        this.randomTicks,
                        this.redstonePower,
                        this.comparatorOutput,
                        this.projectileHit,
                        this.steppedOn,
                        this.landing,
                        this.precipitationTicks,
                        this.blockBreaking,
                        this.scheduledTicks,
                        this.stacksDropped,
                        this.onUse
                );
            }
        }
    }
}
