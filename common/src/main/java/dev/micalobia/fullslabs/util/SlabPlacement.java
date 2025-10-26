package dev.micalobia.fullslabs.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class SlabPlacement {
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


        if (offH > Constants.EDGE_WIDTH || offV > Constants.EDGE_WIDTH) {
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
}
