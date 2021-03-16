package dev.micalobia.full_slabs.util;

import dev.micalobia.full_slabs.block.Blocks;
import dev.micalobia.full_slabs.block.FullSlabBlock;
import dev.micalobia.full_slabs.block.VerticalSlabBlock;
import dev.micalobia.full_slabs.block.enums.SlabState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;

import java.util.Arrays;

public class Helper {
	public static Block fetchBase(Identifier verticalId) {
		String[] split = verticalId.getPath().split("_");
		for(int i = 1; i < split.length; ++i) {
			String namespace = String.join("_", Arrays.copyOfRange(split, 0, i));
			String path = String.join("_", Arrays.copyOfRange(split, i, split.length - 1));
			Identifier check = new Identifier(namespace, path);
			Block block = Helper.fetchBlock(check);
			if(block instanceof SlabBlock) return block;
		}
		throw new RuntimeException("Couldn't rebuild base id from vertical; Report an issue with FullSlabs");
	}

	public static Block fetchBlock(Identifier id) {
		return Registry.BLOCK.get(id);
	}

	public static Block fetchBlock(ModelIdentifier id) {
		return fetchBlock(new Identifier(id.getNamespace(), id.getPath()));
	}

	public static Identifier fetchId(Block block) {
		return Registry.BLOCK.getId(block);
	}

	public static BlockState fetchDefaultState(Identifier id) {
		return fetchBlock(id).getDefaultState();
	}

	public static Item fetchItem(Identifier id) {
		return Registry.ITEM.get(id);
	}

	public static Identifier fetchId(Item item) {
		return Registry.ITEM.getId(item);
	}

	public static Block fetchBlock(Item item) {
		return fetchBlock(fetchId(item));
	}

	public static boolean isVerticalId(Identifier id) {
		if(!"full_slabs".equals(id.getNamespace())) return false;
		return id.getPath().endsWith("_vertical");
	}

	public static boolean isPositive(Vec3d hit, BlockPos pos, Axis axis) {
		switch(axis) {
			case X:
				return hit.getX() - pos.getX() > 0.5;
			case Z:
				return hit.getZ() - pos.getZ() > 0.5;
			default:
				return hit.getY() - pos.getY() > 0.5;
		}
	}

	public static boolean isAnySlab(Block slab) {
		return slab.is(Blocks.FULL_SLAB_BLOCK) || slab instanceof SlabBlock || slab instanceof VerticalSlabBlock;
	}

	public static Axis axisFromSlab(BlockState slab) {
		Block block = slab.getBlock();
		if(block.is(Blocks.FULL_SLAB_BLOCK)) return slab.get(FullSlabBlock.AXIS);
		else if(block instanceof VerticalSlabBlock) return slab.get(VerticalSlabBlock.AXIS);
		else if(block instanceof SlabBlock) return Axis.Y;
		throw new RuntimeException("That isn't a slab!");
	}

	public static boolean isDoubleSlab(BlockState slab) {
		Block block = slab.getBlock();
		if(block.is(Blocks.FULL_SLAB_BLOCK)) return true;
		else if(block instanceof VerticalSlabBlock) return slab.get(VerticalSlabBlock.STATE) == SlabState.DOUBLE;
		else if(block instanceof SlabBlock) return slab.get(SlabBlock.TYPE) == SlabType.DOUBLE;
		throw new RuntimeException("That isn't a slab!");
	}

	public static boolean isPositive(BlockState state) {
		Block block = state.getBlock();
		if(block instanceof VerticalSlabBlock)
			return state.get(VerticalSlabBlock.STATE) == SlabState.POSITIVE;
		else
			return state.get(SlabBlock.TYPE) == SlabType.TOP;
	}

	public static BlockState getState(Block slab, Axis axis, SlabState state) {
		return getState(slab, axis, state, false);
	}

	public static BlockState getState(Block slab, Axis axis, boolean positive) {
		return getState(slab, axis, positive, false);
	}

	public static BlockState getState(Block slab, Axis axis, boolean positive, boolean waterlogged) {
		return getState(slab, axis, positive ? SlabState.POSITIVE : SlabState.NEGATIVE, waterlogged);
	}

	public static BlockState getState(Block slab, Axis axis, SlabState state, boolean waterlogged) {
		if(axis == Axis.Y)
			return LinkedSlabs.horizontal(slab).getDefaultState()
					.with(SlabBlock.TYPE, state.slabType())
					.with(SlabBlock.WATERLOGGED, waterlogged);
		else
			return LinkedSlabs.vertical(slab).getDefaultState()
					.with(VerticalSlabBlock.STATE, state)
					.with(VerticalSlabBlock.AXIS, axis)
					.with(VerticalSlabBlock.WATERLOGGED, waterlogged);
	}

}

