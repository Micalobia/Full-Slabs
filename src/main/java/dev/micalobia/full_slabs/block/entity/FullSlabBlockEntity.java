package dev.micalobia.full_slabs.block.entity;

import dev.micalobia.full_slabs.util.Helper;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.SlabType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;

public class FullSlabBlockEntity extends BlockEntity implements BlockEntityClientSerializable {
	private BlockState positiveState;
	private BlockState negativeState;

	public FullSlabBlockEntity(BlockState positiveState, BlockState negativeState) {
		super(dev.micalobia.full_slabs.block.Blocks.FULL_SLAB_BLOCK_ENTITY);
		this.positiveState = positiveState;
		this.negativeState = negativeState;
	}

	public FullSlabBlockEntity() {
		this(
				Blocks.SMOOTH_STONE_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.TOP),
				Blocks.STONE_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.BOTTOM)
		);
	}

	public BlockState getPositiveState() {
		return positiveState;
	}

	public BlockState getNegativeState() {
		return negativeState;
	}

	public CompoundTag toTag(CompoundTag tag) {
		super.toTag(tag);
		return toClientTag(tag);
	}

	public void fromTag(BlockState state, CompoundTag tag) {
		super.fromTag(state, tag);
		fromClientTag(tag);
	}

	public CompoundTag toClientTag(CompoundTag tag) {
		tag.putString("positive_id", Helper.fetchId(positiveState.getBlock()).toString());
		tag.putString("negative_id", Helper.fetchId(negativeState.getBlock()).toString());
		return tag;
	}

	public void fromClientTag(CompoundTag tag) {
		// TODO: Make it so it actually selects the right state instead of the default one
		positiveState = Helper.fetchDefaultState(new Identifier(tag.getString("positive_id")));
		negativeState = Helper.fetchDefaultState(new Identifier(tag.getString("positive_id")));
	}
}
