package dev.micalobia.full_slabs.block.entity;

import com.mojang.datafixers.util.Pair;
import dev.micalobia.full_slabs.util.Helper;
import dev.micalobia.full_slabs.util.LinkedSlabs;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class FullSlabBlockEntity extends BlockEntity implements BlockEntityClientSerializable, RenderAttachmentBlockEntity {
	private Block positiveSlab;
	private Block negativeSlab;

	public FullSlabBlockEntity(Block positiveSlab, Block negativeSlab) {
		super(dev.micalobia.full_slabs.block.Blocks.FULL_SLAB_BLOCK_ENTITY);
		this.positiveSlab = positiveSlab;
		this.negativeSlab = negativeSlab;
	}

	public FullSlabBlockEntity() {
		this(
				Blocks.SMOOTH_STONE_SLAB,
				Blocks.STONE_SLAB
		);
	}

	public Block getPositiveSlab() {
		return positiveSlab;
	}

	public Block getNegativeSlab() {
		return negativeSlab;
	}

	public Block getSlab(boolean positive) {
		return positive ? positiveSlab : negativeSlab;
	}

	public BlockState getState(Axis axis, boolean positive) {
		return Helper.getState(getSlab(positive), axis, positive);
	}

	public BlockState getHitState(Axis axis, Vec3d hit) {
		boolean isPositive = Helper.isPositive(hit, pos, axis);
		return getState(axis, isPositive);
	}

	public void fromTag(BlockState state, CompoundTag tag) {
		super.fromTag(state, tag);
		fromClientTag(tag);
	}

	public CompoundTag toTag(CompoundTag tag) {
		super.toTag(tag);
		return toClientTag(tag);
	}

	public void fromClientTag(CompoundTag tag) {
		positiveSlab = LinkedSlabs.horizontal(Helper.fetchBlock(new Identifier(tag.getString("positive_id"))));
		negativeSlab = LinkedSlabs.horizontal(Helper.fetchBlock(new Identifier(tag.getString("negative_id"))));
	}

	public CompoundTag toClientTag(CompoundTag tag) {
		tag.putString("positive_id", Helper.fetchId(positiveSlab).toString());
		tag.putString("negative_id", Helper.fetchId(negativeSlab).toString());
		return tag;
	}

	public Block getHitSlab(Vec3d hit, BlockPos pos, Axis axis) {
		return Helper.isPositive(hit, pos, axis) ? getPositiveSlab() : getNegativeSlab();
	}

	public Block getOppositeSlab(Vec3d hit, BlockPos pos, Axis axis) {
		return Helper.isPositive(hit, pos, axis) ? getNegativeSlab() : getPositiveSlab();
	}

	@Override
	public @Nullable Object getRenderAttachmentData() {
		return new Pair<>(positiveSlab, negativeSlab);
	}
}
