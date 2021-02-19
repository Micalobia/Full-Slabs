package dev.micalobia.full_slabs.block.entity;

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.SlabType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

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

	private Identifier fetchId(BlockState state) {
		return Registry.BLOCK.getId(state.getBlock());
	}

	private Block fetchBlock(Identifier id) {
		return Registry.BLOCK.get(id);
	}

	private BlockState fetchDefaultState(Identifier id) {
		return fetchBlock(id).getDefaultState();
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
		tag.putString("positive_id", fetchId(positiveState).toString());
		tag.putString("negative_id", fetchId(negativeState).toString());
		return tag;
	}

	public void fromClientTag(CompoundTag tag) {
		// TODO: Make it so it actually selects the right state instead of the default one
		positiveState = fetchDefaultState(new Identifier(tag.getString("positive_id")));
		negativeState = fetchDefaultState(new Identifier(tag.getString("positive_id")));
	}
}
