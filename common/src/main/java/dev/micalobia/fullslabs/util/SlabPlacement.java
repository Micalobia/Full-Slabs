package dev.micalobia.fullslabs.util;

import dev.micalobia.fullslabs.FullSlabs;
import dev.micalobia.fullslabs.block.VerticalSlabBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SlabPlacement {
    public static Vec2 getLookingAtPosition(Direction blockFace, Direction playerFacing, BlockPos pos, Vec3 hit) {
        var x = (float) (hit.x - pos.getX());
        var y = (float) (hit.y - pos.getY());
        var z = (float) (hit.z - pos.getZ());
        float posH = 0;
        float posV = 0;

        switch (blockFace) {
            case DOWN, UP -> {
                switch (playerFacing) {
                    case NORTH:
                        posH = x;
                        posV = 1f - z;
                        break;
                    case SOUTH:
                        posH = 1f - x;
                        posV = z;
                        break;
                    case WEST:
                        posH = 1f - z;
                        posV = 1f - x;
                        break;
                    case EAST:
                        posH = z;
                        posV = x;
                        break;
                    default:
                }
                if (blockFace == Direction.DOWN) {
                    posV = 1f - posV;
                }
            }
            case NORTH, SOUTH -> {
                posH = blockFace.getAxisDirection() == AxisDirection.POSITIVE ? x : 1f - x;
                posV = y;
            }
            case WEST, EAST -> {
                posH = blockFace.getAxisDirection() == AxisDirection.NEGATIVE ? z : 1f - z;
                posV = y;
            }
        }

        return new Vec2(posH, posV);
    }

    public static Direction getTargetedDirection(Mode mode, Direction face, Direction facing, BlockPos pos, Vec3 hit) {
        Vec2 position = getLookingAtPosition(face, facing, pos, hit);
        return switch (mode) {
            case HYBRID -> getTargetedDirectionHybrid(face, facing, position);
            case VANILLA -> getTargetedDirectionVanilla(face, position.y);
            case VERTICAL -> getTargetedDirectionVertical(face, facing, position);
        };
    }

    private static Direction getTargetedDirectionHybrid(Direction face, Direction facing, Vec2 position) {
        var posH = position.x;
        var posV = position.y;
        var offH = Math.abs(posH - 0.5f);
        var offV = Math.abs(posV - 0.5f);
        if (offH > Constants.EDGE_WIDTH || offV > Constants.EDGE_WIDTH) {
            if (face.getAxis().isVertical()) {
                if (offH > offV) {
                    return posH < 0.5f ? facing.getCounterClockWise() : facing.getClockWise();
                } else {
                    if (face == Direction.DOWN) {
                        return posV > 0.5f ? facing.getOpposite() : facing;
                    } else {
                        return posV < 0.5f ? facing.getOpposite() : facing;
                    }
                }
            } else {
                if (offH > offV) {
                    return posH < 0.5f ? face.getClockWise() : face.getCounterClockWise();
                } else {
                    return posV < 0.5f ? Direction.DOWN : Direction.UP;
                }
            }
        }
        if (face.getAxis().isVertical())
            return face.getOpposite();
        return face;
    }

    private static Direction getTargetedDirectionVanilla(Direction face, float y) {
        if (face == Direction.DOWN) return Direction.UP;
        if (face == Direction.UP) return Direction.DOWN;
        return y > 0.5d ? Direction.UP : Direction.DOWN;
    }

    private static Direction getTargetedDirectionVertical(Direction face, Direction facing, Vec2 position) {
        if (face.getAxis().isVertical()) {
            var direction = position.x < 0.5f ? face.getClockWise(facing.getAxis()) : face.getCounterClockWise(facing.getAxis());
            return facing.getAxisDirection() == AxisDirection.POSITIVE ? direction : direction.getOpposite();
        } else return position.x < 0.5f ? face.getClockWise() : face.getCounterClockWise();
    }

    private static double wrapToMinus180to180(double value) {
        return value < 0d ? 180d - Math.abs(value) % 360d : value - 180d;
    }

    public static BlockState getTargetedState(SlabBlock slab, Direction blockFace, Direction target, double cameraYaw) {
        var vertical = Objects.requireNonNull(VerticalSlabBlock.getVertical(slab));
        return switch (target) {
            case UP -> slab.defaultBlockState().setValue(BlockStateProperties.SLAB_TYPE, SlabType.TOP);
            case DOWN -> slab.defaultBlockState().setValue(BlockStateProperties.SLAB_TYPE, SlabType.BOTTOM);
            default -> {
                var faceAxis = blockFace.getAxis();
                if (faceAxis == target.getAxis())
                    // This is safe to always do away, since the only way it could be towards is if it's doubling up a slab
                    yield vertical.defaultBlockState().setValue(VerticalSlabBlock.TYPE, VerticalSlabBlock.VerticalType.AWAY).setValue(VerticalSlabBlock.DIRECTION, target);
                var altYaw = faceAxis.isVertical() ? target.toYRot() : blockFace.toYRot();
                var delta = wrapToMinus180to180(cameraYaw - altYaw);
                boolean towards;
                if (faceAxis.isVertical()) towards = Math.abs(delta) < 90d;
                else towards = delta < 0d == (blockFace.getCounterClockWise() == target);
                yield vertical.defaultBlockState()
                        .setValue(VerticalSlabBlock.TYPE, towards ? VerticalSlabBlock.VerticalType.TOWARDS : VerticalSlabBlock.VerticalType.AWAY)
                        .setValue(VerticalSlabBlock.DIRECTION, towards ? target : target.getOpposite());
            }
        };
    }

    public enum Mode implements EnumPayload<Mode> {
        HYBRID,
        VANILLA,
        VERTICAL;

        public static final Type<Mode> PACKET_TYPE = new Type<>(FullSlabs.id("mode"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Mode> PACKET_CODEC = EnumPayload.codecOf(Mode.class);

        public Mode next() {
            return switch (this) {
                case HYBRID -> VANILLA;
                case VANILLA -> VERTICAL;
                case VERTICAL -> HYBRID;
            };
        }

        @Override
        @NotNull
        public Type<Mode> type() {
            return PACKET_TYPE;
        }
    }
}
