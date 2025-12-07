package dev.micalobia.fullslabs.fabric.compat.mo_glass;

import dev.micalobia.fullslabs.block.VerticalSlabBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.wurstclient.glass.MoGlassBlocks;
import org.jetbrains.annotations.NotNull;

public class GlassVerticalSlabBlock extends VerticalSlabBlock {
    public GlassVerticalSlabBlock(SlabBlock block, Properties settings) {
        super(block, settings);
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState stateFrom, Direction direction) {
        if (stateFrom.is(Blocks.GLASS)) return true;
        if (stateFrom.is(this)) return isInvisibleToVerticalSlab(state, stateFrom, direction);
        if (stateFrom.is(this.parent)) return isInvisibleToGlassSlab(state, stateFrom, direction);
        if (stateFrom.is(MoGlassBlocks.GLASS_STAIRS)) return isInvisibleToGlassStairs(state, stateFrom, direction);
        return false;
    }

    protected final boolean isInvisibleToVerticalSlab(BlockState state, BlockState stateFrom, Direction direction) {
        var typeFrom = stateFrom.getValue(TYPE);
        if (typeFrom == VerticalType.FULL) return true;
        var directionFrom = getDirection(stateFrom);
        if (directionFrom == direction.getOpposite()) return true;
        var typeSelf = state.getValue(TYPE);
        if (typeSelf == VerticalType.FULL) return false;
        var directionSelf = getDirection(state);
        return directionSelf == directionFrom && directionSelf.getAxis() != direction.getAxis();
    }

    protected final boolean isInvisibleToGlassSlab(BlockState state, BlockState stateFrom, Direction direction) {
        var typeFrom = stateFrom.getValue(BlockStateProperties.SLAB_TYPE);
        if (typeFrom == SlabType.DOUBLE) return true;
        return switch (direction) {
            case DOWN -> typeFrom == SlabType.TOP;
            case UP -> typeFrom == SlabType.BOTTOM;
            case NORTH, SOUTH, WEST, EAST -> false;
        };
    }

    protected final boolean isInvisibleToGlassStairs(BlockState state, BlockState stateFrom, Direction direction) {
        var typeSelf = state.getValue(TYPE);
        var opposite = direction.getOpposite();
        Direction directionSelf;
        if (typeSelf != VerticalType.FULL) {
            directionSelf = getDirection(state);
            // Slab is facing away from the stairs
            if (directionSelf == opposite) return false;
        } else directionSelf = null;
        // Stair is above/below
        var halfFrom = stateFrom.getValue(BlockStateProperties.HALF);
        if (direction == Direction.DOWN) return halfFrom == Half.TOP;
        if (direction == Direction.UP) return halfFrom == Half.BOTTOM;

        // Checks to see whether the left and/or the right side is occluded
        var shapeFrom = stateFrom.getValue(BlockStateProperties.STAIRS_SHAPE);
        var facingFrom = stateFrom.getValue(BlockStateProperties.HORIZONTAL_FACING);

        final boolean leftOccluded;
        final boolean rightOccluded;

        // This section is built from testing in-game with a debug stick
        if (facingFrom == opposite) {
            leftOccluded = shapeFrom != StairsShape.OUTER_LEFT;
            rightOccluded = shapeFrom != StairsShape.OUTER_RIGHT;
        } else if (facingFrom == direction) {
            leftOccluded = shapeFrom == StairsShape.INNER_LEFT;
            rightOccluded = shapeFrom == StairsShape.INNER_RIGHT;
        } else if (facingFrom == opposite.getClockWise()) {
            leftOccluded = shapeFrom != StairsShape.OUTER_RIGHT;
            rightOccluded = shapeFrom == StairsShape.INNER_LEFT;
        } else if (facingFrom == opposite.getCounterClockWise()) {
            leftOccluded = shapeFrom == StairsShape.INNER_RIGHT;
            rightOccluded = shapeFrom != StairsShape.OUTER_LEFT;
        } else
            throw new AssertionError(); // Shouldn't be possible, since facingFrom and direction are both horizontal at this point

        // directionSelf isn't null because it's always set if type isn't full.
        // Full face
        if (typeSelf == VerticalType.FULL || directionSelf == direction) return leftOccluded && rightOccluded;
        // Slab is on the right
        if (directionSelf == direction.getClockWise()) return rightOccluded;
        // Slab is on the left
        if (directionSelf == direction.getCounterClockWise()) return leftOccluded;

        // This shouldn't be possible to get to, but you never know
        throw new AssertionError();
    }

    @Override
    @NotNull
    protected VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter world, BlockPos pos) {
        return 1f;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }
}
