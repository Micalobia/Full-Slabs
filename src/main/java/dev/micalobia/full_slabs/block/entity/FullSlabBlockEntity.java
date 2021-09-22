package dev.micalobia.full_slabs.block.entity;

import com.mojang.datafixers.util.Pair;
import dev.micalobia.full_slabs.FullSlabsMod;
import dev.micalobia.full_slabs.util.Utility;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.SlabType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

public class FullSlabBlockEntity extends BlockEntity implements BlockEntityClientSerializable, RenderAttachmentBlockEntity {
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

	public Block getSlab(boolean positive) {
		return positive ? positiveSlab : negativeSlab;
	}

	private BlockState getPositiveSlabState(Axis axis) {
		return positiveSlab.getDefaultState().with(Properties.AXIS, axis).with(SlabBlock.TYPE, SlabType.TOP);
	}

	private BlockState getNegativeSlabState(Axis axis) {
		return negativeSlab.getDefaultState().with(Properties.AXIS, axis).with(SlabBlock.TYPE, SlabType.BOTTOM);
	}

	public BlockState getSlabState(boolean positive) {
		Axis axis = getCachedState().get(Properties.AXIS);
		return positive ? getPositiveSlabState(axis) : getNegativeSlabState(axis);
	}

	public BlockState getSlabState(Vec3d hit) {
		Axis axis = getCachedState().get(Properties.AXIS);
		boolean positive = Utility.isPositive(axis, hit, pos);
		return positive ? getPositiveSlabState(axis) : getNegativeSlabState(axis);
	}

	public BlockState getOppositeSlabState(Vec3d hit) {
		Axis axis = getCachedState().get(Properties.AXIS);
		boolean positive = Utility.isPositive(axis, hit, pos);
		return positive ? getNegativeSlabState(axis) : getPositiveSlabState(axis);
	}

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
		readCommonNbt(nbt);
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt) {
		super.writeNbt(nbt);
		return writeCommonNbt(nbt);
	}

	@Override
	public void fromClientTag(NbtCompound nbt) {
		readCommonNbt(nbt);
	}

	@Override
	public NbtCompound toClientTag(NbtCompound nbt) {
		return writeCommonNbt(nbt);
	}

	private NbtCompound writeCommonNbt(NbtCompound nbt) {
		nbt.putString("positive_id", Registry.BLOCK.getId(positiveSlab).toString());
		nbt.putString("negative_id", Registry.BLOCK.getId(negativeSlab).toString());
		return nbt;
	}

	private void readCommonNbt(NbtCompound nbt) {
		positiveSlab = Registry.BLOCK.get(new Identifier(nbt.getString("positive_id")));
		negativeSlab = Registry.BLOCK.get(new Identifier(nbt.getString("negative_id")));
		if(!(positiveSlab instanceof SlabBlock)) positiveSlab = Blocks.STONE_SLAB;
		if(!(negativeSlab instanceof SlabBlock)) negativeSlab = Blocks.STONE_SLAB;
	}

	@Override
	public @Nullable Object getRenderAttachmentData() {
		return Pair.of(positiveSlab, negativeSlab);
	}
}
