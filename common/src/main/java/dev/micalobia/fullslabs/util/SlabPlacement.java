package dev.micalobia.fullslabs.util;

import dev.micalobia.fullslabs.FullSlabs;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class SlabPlacement {
    public static Vec2f getLookingAtPosition(Direction blockFace, Direction playerFacing, BlockPos pos, Vec3d hit) {
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
                posH = blockFace.getDirection() == AxisDirection.POSITIVE ? x : 1f - x;
                posV = y;
            }
            case WEST, EAST -> {
                posH = blockFace.getDirection() == AxisDirection.NEGATIVE ? z : 1f - z;
                posV = y;
            }
        }

        return new Vec2f(posH, posV);
    }

    public static Direction getTargetedDirection(Mode mode, Direction face, Direction facing, BlockPos pos, Vec3d hit) {
        Vec2f position = getLookingAtPosition(face, facing, pos, hit);
        return switch (mode) {
            case HYBRID -> getTargetedDirectionHybrid(face, facing, position);
            case VANILLA -> getTargetedDirectionVanilla(face, position.y);
            case VERTICAL -> getTargetedDirectionVertical(face, facing, position);
        };
    }

    private static Direction getTargetedDirectionHybrid(Direction face, Direction facing, Vec2f position) {
        var posH = position.x;
        var posV = position.y;
        var offH = Math.abs(posH - 0.5f);
        var offV = Math.abs(posV - 0.5f);
        if (offH > Constants.EDGE_WIDTH || offV > Constants.EDGE_WIDTH) {
            if (face.getAxis().isVertical()) {
                if (offH > offV) {
                    return posH < 0.5f ? facing.rotateYCounterclockwise() : facing.rotateYClockwise();
                } else {
                    if (face == Direction.DOWN) {
                        return posV > 0.5f ? facing.getOpposite() : facing;
                    } else {
                        return posV < 0.5f ? facing.getOpposite() : facing;
                    }
                }
            } else {
                if (offH > offV) {
                    return posH < 0.5f ? face.rotateYClockwise() : face.rotateYCounterclockwise();
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

    private static Direction getTargetedDirectionVertical(Direction face, Direction facing, Vec2f position) {
        if (face.getAxis().isVertical()) {
            var direction = position.x < 0.5f ? face.rotateClockwise(facing.getAxis()) : face.rotateCounterclockwise(facing.getAxis());
            return facing.getDirection() == AxisDirection.POSITIVE ? direction : direction.getOpposite();
        } else return position.x < 0.5f ? face.rotateYClockwise() : face.rotateYCounterclockwise();
    }

    public enum Mode implements EnumPayload<Mode> {
        HYBRID,
        VANILLA,
        VERTICAL;

        public static final Id<Mode> PACKET_TYPE = new Id<>(FullSlabs.id("mode"));
        public static final PacketCodec<RegistryByteBuf, Mode> PACKET_CODEC = EnumPayload.codecOf(Mode.class);

        public Mode next() {
            return switch (this) {
                case HYBRID -> VANILLA;
                case VANILLA -> VERTICAL;
                case VERTICAL -> HYBRID;
            };
        }

        @Override
        public Id<Mode> getId() {
            return PACKET_TYPE;
        }
    }
}
