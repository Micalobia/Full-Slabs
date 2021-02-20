package dev.micalobia.full_slabs.block;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public interface ISlabBlock {
	@Nullable
	default Direction innerFace(BlockState state) {
		Direction dir = direction(state);
		return dir == null ? null : dir.getOpposite();
	}

	@Nullable Direction direction(BlockState state);

	Axis axis(BlockState state);

	boolean isInside(BlockState state, Vec3d hit, BlockPos pos);

	default boolean isInside(BlockState state, Direction direction) {
		Direction innerFace = innerFace(state);
		if(innerFace == null) return false;
		return innerFace == direction;
	}

	default boolean isInside(BlockState state, Direction direction, Vec3d hit, BlockPos pos) {
		return isInside(state, direction) || isInside(state, hit, pos);
	}
}
