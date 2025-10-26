package dev.micalobia.fullslabs.fabric.compat.mo_glass;

import dev.micalobia.fullslabs.block.VerticalSlabBlock;
import dev.micalobia.fullslabs.util.Utility;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.block.enums.StairShape;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.wurstclient.glass.MoGlassBlocks;

public class GlassVerticalSlabBlock extends VerticalSlabBlock {
    public GlassVerticalSlabBlock(SlabBlock block, Settings settings) {
        super(block, settings);
    }

    @Override
    protected boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
        if (stateFrom.isOf(Blocks.GLASS)) return true;
        if (stateFrom.isOf(this)) return isInvisibleToVerticalSlab(state, stateFrom, direction);
        if (stateFrom.isOf(this.parent)) return isInvisibleToGlassSlab(state, stateFrom, direction);
        if (stateFrom.isOf(MoGlassBlocks.GLASS_STAIRS)) return isInvisibleToGlassStairs(state, stateFrom, direction);
        return false;
    }

    protected final boolean isInvisibleToVerticalSlab(BlockState state, BlockState stateFrom, Direction direction) {
        var typeFrom = stateFrom.get(TYPE);
        if (typeFrom == VerticalType.FULL) return true;
        var directionFrom = Utility.slabDirection(stateFrom);
        if (directionFrom == direction.getOpposite()) return true;
        var typeSelf = state.get(TYPE);
        if (typeSelf == VerticalType.FULL) return false;
        var directionSelf = Utility.slabDirection(state);
        return directionSelf == directionFrom && directionSelf.getAxis() != direction.getAxis();
    }

    protected final boolean isInvisibleToGlassSlab(BlockState state, BlockState stateFrom, Direction direction) {
        var typeFrom = stateFrom.get(Properties.SLAB_TYPE);
        if (typeFrom == SlabType.DOUBLE) return true;
        return switch (direction) {
            case DOWN -> typeFrom == SlabType.TOP;
            case UP -> typeFrom == SlabType.BOTTOM;
            case NORTH, SOUTH, WEST, EAST -> false;
        };
    }

    protected final boolean isInvisibleToGlassStairs(BlockState state, BlockState stateFrom, Direction direction) {
        var typeSelf = state.get(TYPE);
        var opposite = direction.getOpposite();
        Direction directionSelf;
        if (typeSelf != VerticalType.FULL) {
            directionSelf = Utility.slabDirection(state);
            // Slab is facing away from the stairs
            if (directionSelf == opposite) return false;
        } else directionSelf = null;
        // Stair is above/below
        var halfFrom = stateFrom.get(Properties.BLOCK_HALF);
        if (direction == Direction.DOWN) return halfFrom == BlockHalf.TOP;
        if (direction == Direction.UP) return halfFrom == BlockHalf.BOTTOM;

        // Checks to see whether the left and/or the right side is occluded
        var shapeFrom = stateFrom.get(Properties.STAIR_SHAPE);
        var facingFrom = stateFrom.get(Properties.HORIZONTAL_FACING);

        final boolean leftOccluded;
        final boolean rightOccluded;

        // This section is built from testing in-game with a debug stick
        if (facingFrom == opposite) {
            leftOccluded = shapeFrom != StairShape.OUTER_LEFT;
            rightOccluded = shapeFrom != StairShape.OUTER_RIGHT;
        } else if (facingFrom == direction) {
            leftOccluded = shapeFrom == StairShape.INNER_LEFT;
            rightOccluded = shapeFrom == StairShape.INNER_RIGHT;
        } else if (facingFrom == opposite.rotateYClockwise()) {
            leftOccluded = shapeFrom != StairShape.OUTER_RIGHT;
            rightOccluded = shapeFrom == StairShape.INNER_LEFT;
        } else if (facingFrom == opposite.rotateYCounterclockwise()) {
            leftOccluded = shapeFrom == StairShape.INNER_RIGHT;
            rightOccluded = shapeFrom != StairShape.OUTER_LEFT;
        } else
            throw new AssertionError(); // Shouldn't be possible, since facingFrom and direction are both horizontal at this point

        // directionSelf isn't null because it's always set if type isn't full.
        // Full face
        if (typeSelf == VerticalType.FULL || directionSelf == direction) return leftOccluded && rightOccluded;
        // Slab is on the right
        if (directionSelf == direction.rotateYClockwise()) return rightOccluded;
        // Slab is on the left
        if (directionSelf == direction.rotateYCounterclockwise()) return leftOccluded;

        // This shouldn't be possible to get to, but you never know
        throw new AssertionError();
    }

    @Override
    protected VoxelShape getCameraCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.empty();
    }

    @Override
    protected float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
        return 1f;
    }

    @Override
    protected boolean isTransparent(BlockState state) {
        return true;
    }
}
