package dev.micalobia.fullslabs.util;

import com.google.common.collect.ImmutableList;
import dev.micalobia.fullslabs.block.SlabLike;
import dev.micalobia.fullslabs.block.VerticalSlabBlock;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.Vec3;

import java.util.List;

@MethodsReturnNonnullByDefault
public enum MixedType implements StringRepresentable {
    NORTH("north", Direction.NORTH),
    SOUTH("south", Direction.SOUTH),
    EAST("east", Direction.EAST),
    WEST("west", Direction.WEST),
    VERTICAL("vertical", Direction.UP);

    private static final List<MixedType> CARDINAL = ImmutableList.of(
            NORTH, SOUTH, EAST, WEST
    );
    public final Direction direction;
    private final String name;

    MixedType(String name, Direction direction) {
        this.name = name;
        this.direction = direction;
    }

    public static List<MixedType> cardinal() {
        return CARDINAL;
    }

    public static MixedType fromState(BlockState state) {
        if (!(state.getBlock() instanceof SlabLike slab))
            throw new IllegalArgumentException("Not a slab!");
        return slab.getType(state);
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public BlockState state(SlabBlock slab, boolean towards) {
        if (!VerticalSlabBlock.hasVertical(slab))
            throw new IllegalArgumentException("%s is missing a vertical".formatted(slab));
        if (this == VERTICAL)
            return slab.defaultBlockState().setValue(BlockStateProperties.SLAB_TYPE, towards ? SlabType.TOP : SlabType.BOTTOM);
        return VerticalSlabBlock.getVertical(slab).defaultBlockState().setValue(VerticalSlabBlock.TYPE, towards ? VerticalSlabBlock.VerticalType.TOWARDS : VerticalSlabBlock.VerticalType.AWAY).setValue(BlockStateProperties.HORIZONTAL_FACING, this.direction);
    }

    public boolean isAxisTargetTowards(Vec3 pos, BlockPos location) {
        return switch (this.direction.getAxis()) {
            case X -> pos.x - location.getX() > 0.5d ? Direction.EAST : Direction.WEST;
            case Y -> pos.y - location.getY() > 0.5d ? Direction.UP : Direction.DOWN;
            case Z -> pos.z - location.getZ() > 0.5d ? Direction.SOUTH : Direction.NORTH;
        } == this.direction;
    }

}
