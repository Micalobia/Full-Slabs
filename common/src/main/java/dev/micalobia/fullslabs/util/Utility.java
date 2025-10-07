package dev.micalobia.fullslabs.util;

import dev.micalobia.fullslabs.SlabRegistry;
import dev.micalobia.fullslabs.block.MixedSlabBlock;
import dev.micalobia.fullslabs.block.MixedSlabBlock.MixedType;
import dev.micalobia.fullslabs.block.VerticalSlabBlock;
import dev.micalobia.fullslabs.block.VerticalSlabBlock.VerticalType;
import dev.micalobia.fullslabs.config.Config;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.HoneycombItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class Utility {
    private static double edgeWidth() {
        return Config.edgeWidth;
    }

    public static Vec3d getLookingAtPosition(Direction blockFace, Direction playerFacing, BlockPos pos, Vec3d hit) {
        var x = hit.x - pos.getX();
        var y = hit.y - pos.getY();
        var z = hit.z - pos.getZ();
        double posH = 0;
        double posV = 0;

        switch (blockFace) {
            case DOWN, UP -> {
                switch (playerFacing) {
                    case NORTH:
                        posH = x;
                        posV = 1.0d - z;
                        break;
                    case SOUTH:
                        posH = 1.0d - x;
                        posV = z;
                        break;
                    case WEST:
                        posH = 1.0d - z;
                        posV = 1.0d - x;
                        break;
                    case EAST:
                        posH = z;
                        posV = x;
                        break;
                    default:
                }
                if (blockFace == Direction.DOWN) {
                    posV = 1.0d - posV;
                }
            }
            case NORTH, SOUTH -> {
                posH = blockFace.getDirection() == Direction.AxisDirection.POSITIVE ? x : 1.0d - x;
                posV = y;
            }
            case WEST, EAST -> {
                posH = blockFace.getDirection() == Direction.AxisDirection.NEGATIVE ? z : 1.0d - z;
                posV = y;
            }
        }

        return new Vec3d(posH, posV, 0);
    }

    public static Direction getTargetedDirection(Direction blockFace, Direction playerFacing, BlockPos pos, Vec3d hit) {
        Vec3d positions = getLookingAtPosition(blockFace, playerFacing, pos, hit);
        double posH = positions.x;
        double posV = positions.y;
        var offH = Math.abs(posH - 0.5d);
        var offV = Math.abs(posV - 0.5d);

        if (offH > edgeWidth() || offV > edgeWidth()) {
            if (blockFace.getAxis().isVertical()) {
                if (offH > offV) {
                    return posH < 0.5d ? playerFacing.rotateYCounterclockwise() : playerFacing.rotateYClockwise();
                } else {
                    if (blockFace == Direction.DOWN) {
                        return posV > 0.5d ? playerFacing.getOpposite() : playerFacing;
                    } else {
                        return posV < 0.5d ? playerFacing.getOpposite() : playerFacing;
                    }
                }
            } else {
                if (offH > offV) {
                    return posH < 0.5d ? blockFace.rotateYClockwise() : blockFace.rotateYCounterclockwise();
                } else {
                    return posV < 0.5d ? Direction.DOWN : Direction.UP;
                }
            }
        }
        if (blockFace.getAxis().isVertical())
            return blockFace.getOpposite();
        return blockFace;
    }

    private static double wrapToMinus180to180(double value) {
        return value < 0d ? 180d - Math.abs(value) % 360d : value - 180d;
    }

    public static BlockState getTargetedState(SlabBlock slab, Direction blockFace, Direction target, double cameraYaw) {
        var vertical = VerticalSlabBlock.getVertical(slab);
        return switch (target) {
            case UP -> slab.getDefaultState().with(Properties.SLAB_TYPE, SlabType.TOP);
            case DOWN -> slab.getDefaultState().with(Properties.SLAB_TYPE, SlabType.BOTTOM);
            default -> {
                var faceAxis = blockFace.getAxis();
                if (faceAxis == target.getAxis())
                    // This is safe to always do away, since the only way it could be towards is if it's doubling up a slab
                    yield vertical.getDefaultState().with(VerticalSlabBlock.TYPE, VerticalType.AWAY).with(VerticalSlabBlock.DIRECTION, target);
                var altYaw = faceAxis.isVertical() ? target.getPositiveHorizontalDegrees() : blockFace.getPositiveHorizontalDegrees();
                var delta = wrapToMinus180to180(cameraYaw - altYaw);
                boolean towards;
                if (faceAxis.isVertical()) towards = Math.abs(delta) < 90d;
                else towards = delta < 0d == (blockFace.rotateYCounterclockwise() == target);
                yield vertical.getDefaultState()
                        .with(VerticalSlabBlock.TYPE, towards ? VerticalType.TOWARDS : VerticalType.AWAY)
                        .with(VerticalSlabBlock.DIRECTION, towards ? target : target.getOpposite());
            }
        };
    }

    public static boolean isSlabWithVertical(ItemStack stack) {
        return isSlabWithVertical(stack.getItem());
    }

    public static boolean isSlabWithVertical(Item item) {
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

    public static boolean isSlab(Item item) {
        return item instanceof BlockItem blockItem && isSlab(blockItem.getBlock());
    }

    public static boolean isSlab(BlockState state) {
        return isSlab(state.getBlock());
    }

    public static boolean isSlab(Block block) {
        return block instanceof SlabBlock || block instanceof VerticalSlabBlock;
    }

    public static boolean isDoubleSlab(BlockState state) {
        return state.getBlock() instanceof SlabBlock && state.get(Properties.SLAB_TYPE) == SlabType.DOUBLE ||
                state.getBlock() instanceof VerticalSlabBlock && state.get(VerticalSlabBlock.TYPE) == VerticalType.FULL;
    }

    public static boolean isInsideSlab(BlockState state, BlockPos blockPos, Vec3d hitPos) {
        var block = state.getBlock();
        if (!isSlab(block)) return false;
        if (block instanceof SlabBlock) {
            var type = state.get(Properties.SLAB_TYPE);
            if (type == SlabType.DOUBLE) return false;
            var diff = hitPos.y - blockPos.getY();
            return type == SlabType.BOTTOM ? diff >= 0.5d : diff <= 0.5d;
        }
        var type = state.get(VerticalSlabBlock.TYPE);
        if (type == VerticalType.FULL) return false;
        var dir = state.get(VerticalSlabBlock.DIRECTION);
        dir = type == VerticalType.TOWARDS ? dir : dir.getOpposite();
        return switch (dir) {
            case NORTH -> hitPos.z - blockPos.getZ() >= 0.5d;
            case SOUTH -> hitPos.z - blockPos.getZ() <= 0.5d;
            case WEST -> hitPos.x - blockPos.getX() >= 0.5d;
            case EAST -> hitPos.x - blockPos.getX() <= 0.5d;
            default -> false;
        };
    }

    public static HitResult crosshair(PlayerEntity player) {
        return player.raycast(player.getBlockInteractionRange(), 1f, false);
    }

    public static @Nullable StatePair breakHalf(BlockView view, BlockState state, BlockPos pos, HitResult crosshair) {
        Objects.requireNonNull(view);
        Objects.requireNonNull(state);
        Objects.requireNonNull(pos);
        Objects.requireNonNull(crosshair);
        var hit = crosshair.getPos();
        var block = state.getBlock();
        var mixed = SlabRegistry.MIXED_SLAB.get();
        if (state.isOf(mixed)) {
            var type = state.get(MixedSlabBlock.TYPE);
            var towards = type.isAxisTargetTowards(hit, pos);
            return mixed.act(view, pos, entity -> {
                return new StatePair(entity.getState(towards), entity.getState(!towards));
            });
        }
        if (!isDoubleSlab(state)) return null;
        var type = MixedType.fromState(state);
        var towards = type.isAxisTargetTowards(hit, pos);
        if (block instanceof SlabBlock) return new StatePair(
                state.with(Properties.SLAB_TYPE, towards ? SlabType.TOP : SlabType.BOTTOM),
                state.with(Properties.SLAB_TYPE, towards ? SlabType.BOTTOM : SlabType.TOP)
        );
        if (block instanceof VerticalSlabBlock) return new StatePair(
                state.with(VerticalSlabBlock.TYPE, towards ? VerticalType.TOWARDS : VerticalType.AWAY),
                state.with(VerticalSlabBlock.TYPE, towards ? VerticalType.AWAY : VerticalType.TOWARDS)
        );
        throw new AssertionError();
    }

    public static Optional<Block> getWaxed(Block unwaxed) {
        return Optional.ofNullable(HoneycombItem.UNWAXED_TO_WAXED_BLOCKS.get().get(unwaxed));
    }

    public record StatePair(BlockState towards, BlockState away) {}
}

