package dev.micalobia.full_slabs.util;

import dev.micalobia.full_slabs.util.malilib.HitPart;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class Utility {
	public static HitResult crosshair(PlayerEntity player) {
		return player.raycast(player.isCreative() ? 5.0d : 4.5d, 0f, false);
	}

	public static Identifier getBlockId(Block block) {
		return Registry.BLOCK.getId(block);
	}

	public static Block getBlock(Identifier id) {
		return Registry.BLOCK.get(id);
	}

	public static BlockState getStateFromString(Block block, @Nullable String string) {
		if(string == null) return null;
		StateManager<Block, BlockState> manager = block.getStateManager();
		String[] properties = string.split(",");
		BlockState state = block.getDefaultState();
		for(String str : properties) {
			String[] pair = str.split("=");
			String name = pair[0];
			if(pair.length != 2) continue;
			String value = pair[1];
			Property<?> property = manager.getProperty(name);
			if(property == null) continue;
			state = with(state, property, value);
		}
		return state;
	}

	private static <T extends Comparable<T>> BlockState with(BlockState state, Property<T> property, String value) {
		Optional<T> ret = property.parse(value);
		if(ret.isPresent()) return state.with(property, ret.get());
		return state;
	}

	// All code below exists because of a linkage error that I need to fix somehow, as well as util.malilib

	public static HitPart getHitPart(Direction originalSide, Direction playerFacingH, BlockPos pos, Vec3d hitVec) {
		Vec3d positions = getHitPartPositions(originalSide, playerFacingH, pos, hitVec);
		double posH = positions.x;
		double posV = positions.y;
		double offH = Math.abs(posH - 0.5d);
		double offV = Math.abs(posV - 0.5d);

		if(offH > 0.25d || offV > 0.25d) {
			if(offH > offV) {
				return posH < 0.5d ? HitPart.LEFT : HitPart.RIGHT;
			} else {
				return posV < 0.5d ? HitPart.BOTTOM : HitPart.TOP;
			}
		} else {
			return HitPart.CENTER;
		}
	}

	private static Vec3d getHitPartPositions(Direction originalSide, Direction playerFacingH, BlockPos pos, Vec3d hitVec) {
		double x = hitVec.x - pos.getX();
		double y = hitVec.y - pos.getY();
		double z = hitVec.z - pos.getZ();
		double posH = 0;
		double posV = 0;

		switch(originalSide) {
			case DOWN, UP -> {
				switch(playerFacingH) {
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
				if(originalSide == Direction.DOWN) {
					posV = 1.0d - posV;
				}
			}
			case NORTH, SOUTH -> {
				posH = originalSide.getDirection() == AxisDirection.POSITIVE ? x : 1.0d - x;
				posV = y;
			}
			case WEST, EAST -> {
				posH = originalSide.getDirection() == AxisDirection.NEGATIVE ? z : 1.0d - z;
				posV = y;
			}
		}

		return new Vec3d(posH, posV, 0);
	}

	public static Direction getTargetedDirection(Direction side, Direction playerFacingH, BlockPos pos, Vec3d hitVec) {
		Vec3d positions = getHitPartPositions(side, playerFacingH, pos, hitVec);
		double posH = positions.x;
		double posV = positions.y;
		double offH = Math.abs(posH - 0.5d);
		double offV = Math.abs(posV - 0.5d);

		if(offH > 0.25d || offV > 0.25d) {
			if(side.getAxis() == Direction.Axis.Y) {
				if(offH > offV) {
					return posH < 0.5d ? playerFacingH.rotateYCounterclockwise() : playerFacingH.rotateYClockwise();
				} else {
					if(side == Direction.DOWN) {
						return posV > 0.5d ? playerFacingH.getOpposite() : playerFacingH;
					} else {
						return posV < 0.5d ? playerFacingH.getOpposite() : playerFacingH;
					}
				}
			} else {
				if(offH > offV) {
					return posH < 0.5d ? side.rotateYClockwise() : side.rotateYCounterclockwise();
				} else {
					return posV < 0.5d ? Direction.DOWN : Direction.UP;
				}
			}
		}

		return side;
	}
}
