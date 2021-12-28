package dev.micalobia.full_slabs.block.entity;

import dev.micalobia.full_slabs.FullSlabsMod;
import dev.micalobia.full_slabs.block.SlabBlockUtility;
import dev.micalobia.micalibria.block.entity.MBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;

public class FullSlabBlockEntity extends MBlockEntity {
	private Block positiveSlab;
	private Block negativeSlab;

	public FullSlabBlockEntity(BlockPos pos, BlockState state, Block positive, Block negative) {
		super(FullSlabsMod.FULL_SLAB_BLOCK_ENTITY, pos, state);
		positiveSlab = positive;
		negativeSlab = negative;
	}

	public FullSlabBlockEntity(BlockPos pos, BlockState state) {
		this(pos, state, Blocks.STONE_SLAB, Blocks.STONE_SLAB);
	}

	private BlockState getPositiveSlabState(Axis axis) {
		return positiveSlab.getDefaultState().with(Properties.AXIS, axis).with(SlabBlock.TYPE, SlabType.TOP);
	}

	public BlockState getPositiveSlabState() {
		return positiveSlab.getDefaultState().with(Properties.AXIS, getCachedState().get(Properties.AXIS)).with(SlabBlock.TYPE, SlabType.TOP);
	}

	private BlockState getNegativeSlabState(Axis axis) {
		return negativeSlab.getDefaultState().with(Properties.AXIS, axis).with(SlabBlock.TYPE, SlabType.BOTTOM);
	}

	public BlockState getNegativeSlabState() {
		return negativeSlab.getDefaultState().with(Properties.AXIS, getCachedState().get(Properties.AXIS)).with(SlabBlock.TYPE, SlabType.BOTTOM);
	}

	public BlockState getSlabState(Vec3d hit) {
		Axis axis = getCachedState().get(Properties.AXIS);
		boolean positive = SlabBlockUtility.isPositive(axis, hit, pos);
		return positive ? getPositiveSlabState(axis) : getNegativeSlabState(axis);
	}

	public BlockState getOppositeSlabState(Vec3d hit) {
		Axis axis = getCachedState().get(Properties.AXIS);
		boolean positive = SlabBlockUtility.isPositive(axis, hit, pos);
		return positive ? getNegativeSlabState(axis) : getPositiveSlabState(axis);
	}

	@Override
	public void writeToNbt(NbtCompound nbt) {
		nbt.putString("positive_id", Registry.BLOCK.getId(positiveSlab).toString());
		nbt.putString("negative_id", Registry.BLOCK.getId(negativeSlab).toString());
	}

	@Override
	public void readFromNbt(NbtCompound nbt) {
		positiveSlab = Registry.BLOCK.get(new Identifier(nbt.getString("positive_id")));
		negativeSlab = Registry.BLOCK.get(new Identifier(nbt.getString("negative_id")));
		if(!(positiveSlab instanceof SlabBlock)) positiveSlab = Blocks.STONE_SLAB;
		if(!(negativeSlab instanceof SlabBlock)) negativeSlab = Blocks.STONE_SLAB;
	}

	@Override
	public void writeToClientNbt(NbtCompound nbt) {
		nbt.putString("positive_id", Registry.BLOCK.getId(positiveSlab).toString());
		nbt.putString("negative_id", Registry.BLOCK.getId(negativeSlab).toString());
	}

	@Override
	public void readFromClientNbt(NbtCompound nbt) {
		positiveSlab = Registry.BLOCK.get(new Identifier(nbt.getString("positive_id")));
		negativeSlab = Registry.BLOCK.get(new Identifier(nbt.getString("negative_id")));
		if(!(positiveSlab instanceof SlabBlock)) positiveSlab = Blocks.STONE_SLAB;
		if(!(negativeSlab instanceof SlabBlock)) negativeSlab = Blocks.STONE_SLAB;
	}
}
