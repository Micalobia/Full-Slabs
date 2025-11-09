package dev.micalobia.fullslabs.util;

import dev.micalobia.fullslabs.SlabRegistry;
import dev.micalobia.fullslabs.block.MixedSlabBlock;
import dev.micalobia.fullslabs.block.MixedSlabBlock.MixedType;
import dev.micalobia.fullslabs.block.VerticalSlabBlock;
import dev.micalobia.fullslabs.block.VerticalSlabBlock.VerticalType;
import dev.micalobia.fullslabs.handlers.MixedContext;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("unused")
public class Utility {
    private static double wrapToMinus180to180(double value) {
        return value < 0d ? 180d - Math.abs(value) % 360d : value - 180d;
    }

    public static BlockState getTargetedState(SlabBlock slab, Direction blockFace, Direction target, double cameraYaw) {
        var vertical = VerticalSlabBlock.getVertical(slab);
        return switch (target) {
            case UP -> slab.defaultBlockState().setValue(BlockStateProperties.SLAB_TYPE, SlabType.TOP);
            case DOWN -> slab.defaultBlockState().setValue(BlockStateProperties.SLAB_TYPE, SlabType.BOTTOM);
            default -> {
                var faceAxis = blockFace.getAxis();
                if (faceAxis == target.getAxis())
                    // This is safe to always do away, since the only way it could be towards is if it's doubling up a slab
                    yield vertical.defaultBlockState().setValue(VerticalSlabBlock.TYPE, VerticalType.AWAY).setValue(VerticalSlabBlock.DIRECTION, target);
                var altYaw = faceAxis.isVertical() ? target.toYRot() : blockFace.toYRot();
                var delta = wrapToMinus180to180(cameraYaw - altYaw);
                boolean towards;
                if (faceAxis.isVertical()) towards = Math.abs(delta) < 90d;
                else towards = delta < 0d == (blockFace.getCounterClockWise() == target);
                yield vertical.defaultBlockState()
                        .setValue(VerticalSlabBlock.TYPE, towards ? VerticalType.TOWARDS : VerticalType.AWAY)
                        .setValue(VerticalSlabBlock.DIRECTION, towards ? target : target.getOpposite());
            }
        };
    }

    public static boolean isSlabWithVertical(ItemStack stack) {
        return isSlabWithVertical(stack.getItem());
    }

    public static boolean isSlabWithVertical(ItemLike item) {
        return item instanceof BlockItem blockItem && isSlabWithVertical(blockItem.getBlock());
    }

    public static boolean isSlabWithVertical(BlockState state) {
        return isSlabWithVertical(state.getBlock());
    }

    public static boolean isSlabWithVertical(Block block) {
        return block instanceof VerticalSlabBlock || block instanceof SlabBlock slab && VerticalSlabBlock.hasVertical(slab);
    }

    public static boolean isSlab(ItemStack stack) {
        return isSlab(stack.getItem());
    }

    public static boolean isSlab(ItemLike item) {
        return item instanceof BlockItem blockItem && isSlab(blockItem.getBlock());
    }

    public static boolean isSlab(BlockState state) {
        return isSlab(state.getBlock());
    }

    public static boolean isSlab(Block block) {
        return block instanceof SlabBlock || block instanceof VerticalSlabBlock;
    }

    public static boolean isDoubleSlab(BlockState state) {
        return state.getBlock() instanceof SlabBlock && state.getValue(BlockStateProperties.SLAB_TYPE) == SlabType.DOUBLE ||
                state.getBlock() instanceof VerticalSlabBlock && state.getValue(VerticalSlabBlock.TYPE) == VerticalType.FULL;
    }

    public static boolean isInsideSlab(BlockState state, BlockPos pos, Vec3 hit) {
        var block = state.getBlock();
        if (!isSlab(block)) return false;
        if (block instanceof SlabBlock) {
            var type = state.getValue(BlockStateProperties.SLAB_TYPE);
            if (type == SlabType.DOUBLE) return false;
            var diff = hit.y - pos.getY();
            return type == SlabType.BOTTOM ? diff >= 0.5d : diff <= 0.5d;
        }
        var type = state.getValue(VerticalSlabBlock.TYPE);
        if (type == VerticalType.FULL) return false;
        var dir = state.getValue(VerticalSlabBlock.DIRECTION);
        dir = type == VerticalType.TOWARDS ? dir : dir.getOpposite();
        return switch (dir) {
            case NORTH -> hit.z - pos.getZ() >= 0.5d;
            case SOUTH -> hit.z - pos.getZ() <= 0.5d;
            case WEST -> hit.x - pos.getX() >= 0.5d;
            case EAST -> hit.x - pos.getX() <= 0.5d;
            default -> false;
        };
    }

    public static HitResult crosshair(Player player) {
        return player.pick(player.blockInteractionRange(), 1f, false);
    }

    public static HitResult crosshair(@Nullable Player player, boolean isClient) {
        if (isClient) return Minecraft.getInstance().hitResult;
        if (player == null) throw new IllegalArgumentException("Player is null on serverside!");
        return crosshair(player);
    }

    public static @Nullable StatePair breakHalf(BlockGetter view, BlockState state, BlockPos pos, HitResult crosshair) {
        Objects.requireNonNull(view);
        Objects.requireNonNull(state);
        Objects.requireNonNull(pos);
        Objects.requireNonNull(crosshair);
        var hit = crosshair.getLocation();
        var block = state.getBlock();
        var mixed = SlabRegistry.MIXED_SLAB.get();
        if (state.is(mixed)) {
            var type = state.getValue(MixedSlabBlock.TYPE);
            var towards = type.isAxisTargetTowards(hit, pos);
            return mixed.forward(view, pos, ctx -> {
                var entity = ctx.blockEntityOrThrow();
                return new StatePair(entity.getState(towards), entity.getState(!towards));
            });
        }
        if (!isDoubleSlab(state)) return null;
        var type = MixedType.fromState(state);
        var towards = type.isAxisTargetTowards(hit, pos);
        if (block instanceof SlabBlock) return new StatePair(
                state.setValue(BlockStateProperties.SLAB_TYPE, towards ? SlabType.TOP : SlabType.BOTTOM),
                state.setValue(BlockStateProperties.SLAB_TYPE, towards ? SlabType.BOTTOM : SlabType.TOP)
        );
        if (block instanceof VerticalSlabBlock) return new StatePair(
                state.setValue(VerticalSlabBlock.TYPE, towards ? VerticalType.TOWARDS : VerticalType.AWAY),
                state.setValue(VerticalSlabBlock.TYPE, towards ? VerticalType.AWAY : VerticalType.TOWARDS)
        );
        throw new AssertionError();
    }

    public static BlockState targetedHalf(BlockGetter world, BlockState state, BlockPos pos, Vec3 hit) {
        var mixed = SlabRegistry.MIXED_SLAB.get();
        if (!(Utility.isDoubleSlab(state) || state.is(mixed))) return state;
        var block = state.getBlock();
        if (block == mixed) return mixed.forwardSideValue(world, pos, hit, MixedContext.Sided::state);
        var towards = MixedType.fromState(state).isAxisTargetTowards(hit, pos);
        if (block instanceof SlabBlock)
            return state.setValue(BlockStateProperties.SLAB_TYPE, towards ? SlabType.TOP : SlabType.BOTTOM);
        return state.setValue(VerticalSlabBlock.TYPE, towards ? VerticalType.TOWARDS : VerticalType.AWAY);
    }

    public static Direction slabDirection(BlockState state) {
        switch (state.getBlock()) {
            case SlabBlock ignored -> {
                var type = state.getValue(BlockStateProperties.SLAB_TYPE);
                if (type == SlabType.DOUBLE) throw new IllegalArgumentException("Not a half-slab!");
                return type == SlabType.TOP ? Direction.UP : Direction.DOWN;
            }
            case VerticalSlabBlock ignored -> {
                var type = state.getValue(VerticalSlabBlock.TYPE);
                if (type == VerticalType.FULL) throw new IllegalArgumentException("Not a half-slab!");
                var direction = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
                return type == VerticalType.TOWARDS ? direction : direction.getOpposite();
            }
            default -> throw new IllegalArgumentException("Not a half-slab!");
        }
    }

    public static Optional<Block> getWaxed(Block unwaxed) {
        return Optional.ofNullable(HoneycombItem.WAXABLES.get().get(unwaxed));
    }

    public record StatePair(BlockState towards, BlockState away) {}
}

