package dev.micalobia.full_slabs.block.entity;

import com.mojang.datafixers.util.Pair;
import dev.micalobia.full_slabs.FullSlabsMod;
import dev.micalobia.full_slabs.block.ExtraSlabBlock;
import dev.micalobia.full_slabs.config.ModConfig;
import dev.micalobia.full_slabs.config.SlabExtra;
import dev.micalobia.full_slabs.mixin.item.WallStandingBlockItemAccessor;
import dev.micalobia.full_slabs.util.Utility;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.SlabType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.WallStandingBlockItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ExtraSlabBlockEntity extends BlockEntity implements BlockEntityClientSerializable, RenderAttachmentBlockEntity {
	public static final Map<Identifier, SlabExtra> allowedExtras;

	static {
		allowedExtras = AutoConfig.getConfigHolder(ModConfig.class).getConfig().getSlabExtras();
	}

	private SlabExtra extra;
	private Block base;

	public ExtraSlabBlockEntity(BlockPos pos, BlockState state, Block base, BlockItem item) {
		super(FullSlabsMod.EXTRA_SLAB_BLOCK_ENTITY, pos, state);
		Block extraBlock;
		if(state.get(ExtraSlabBlock.AXIS).isHorizontal() && item instanceof WallStandingBlockItem wallItem)
			extraBlock = ((WallStandingBlockItemAccessor) wallItem).getWallBlock();
		else
			extraBlock = item.getBlock();
		if(allowed(extraBlock)) this.extra = allowedExtras.get(Utility.getBlockId(extraBlock));
		else throw new RuntimeException("Not a valid extra");
		if(base instanceof SlabBlock)
			this.base = base;
		else
			this.base = Blocks.SMOOTH_STONE_SLAB;
	}

	public ExtraSlabBlockEntity(BlockPos pos, BlockState state) {
		super(FullSlabsMod.EXTRA_SLAB_BLOCK_ENTITY, pos, state);
		if(allowedExtras.keySet().isEmpty()) {
			extra = null;
			base = null;
		} else {
			extra = allowedExtras.values().stream().findFirst().get();
			base = Blocks.SMOOTH_STONE_SLAB;
		}
	}

	public static boolean allowed(Block block) {
		return allowedExtras.containsKey(Utility.getBlockId(block));
	}

	public static boolean allowed(BlockState state, BlockItem extra) {
		Axis axis = state.get(ExtraSlabBlock.AXIS);
		SlabType type = state.get(ExtraSlabBlock.TYPE);
		Direction direction = Utility.getDirection(type, axis);
		if(axis.isHorizontal() && extra instanceof WallStandingBlockItem wallItem) {
			Block wallBlock = ((WallStandingBlockItemAccessor) wallItem).getWallBlock();
			return allowed(wallBlock) && allowedExtras.get(Utility.getBlockId(wallBlock)).allowed(direction);
		} else
			return allowed(extra.getBlock()) && allowedExtras.get(Utility.getBlockId(extra.getBlock())).allowed(direction);
	}

	public static @Nullable SlabExtra get(Block block) {
		return allowedExtras.get(Utility.getBlockId(block));
	}

	public static @Nullable SlabExtra get(Axis axis, BlockItem item) {
		if(axis.isHorizontal() && item instanceof WallStandingBlockItem wallItem) {
			return get(((WallStandingBlockItemAccessor) wallItem).getWallBlock());
		}
		return get(item.getBlock());
	}

	protected SlabExtra getExtra() {
		return this.extra;
	}

	public BlockState getBaseState() {
		BlockState state = getCachedState();
		return getBase()
				.getDefaultState()
				.with(Properties.AXIS, state.get(ExtraSlabBlock.AXIS))
				.with(SlabBlock.TYPE, state.get(ExtraSlabBlock.TYPE));
	}

	public BlockState getExtraState() {
		BlockState state = getCachedState();
		SlabType type = state.get(ExtraSlabBlock.TYPE);
		Axis axis = state.get(ExtraSlabBlock.AXIS);
		return getExtra().getState(Utility.getDirection(type, axis));
	}

	public VoxelShape getBaseOutlineShape(BlockView world, BlockPos pos, ShapeContext context) {
		return getBaseState().getOutlineShape(world, pos, context);
	}

	public VoxelShape getBaseCollisionShape(BlockView world, BlockPos pos, ShapeContext context) {
		return getBaseState().getCollisionShape(world, pos, context);
	}

	public VoxelShape getExtraOutlineShape(BlockView world, BlockPos pos, ShapeContext context) {
		return getExtra().getOutlineShape(ExtraSlabBlock.getDirection(getCachedState()), world, pos, context);
	}

	public VoxelShape getExtraCollisionShape(BlockView world, BlockPos pos, ShapeContext context) {
		return getExtra().getCollisionShape(ExtraSlabBlock.getDirection(getCachedState()), world, pos, context);
	}

	protected Block getBase() {
		return this.base;
	}

	public boolean waterloggable() {
		return this.getExtra().waterloggable();
	}

	@Override
	public void readNbt(NbtCompound nbt) {
		readCommonNbt(nbt);
		super.readNbt(nbt);
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt) {
		writeCommonNbt(nbt);
		return super.writeNbt(nbt);
	}

	private void readCommonNbt(NbtCompound nbt) {
		this.base = Utility.getBlock(new Identifier(nbt.getString("base_identifier")));
		if(!(this.base instanceof SlabBlock))
			this.base = Blocks.SMOOTH_STONE_SLAB;
		Identifier extra = new Identifier(nbt.getString("extra_identifier"));
		this.extra = allowedExtras.getOrDefault(extra, null);
	}

	private NbtCompound writeCommonNbt(NbtCompound nbt) {
		nbt.putString("base_identifier", Utility.getBlockId(this.base).toString());
		nbt.putString("extra_identifier", Utility.getBlockId(this.extra.getBlock()).toString());
		return nbt;
	}

	@Override
	public void fromClientTag(NbtCompound nbt) {
		readCommonNbt(nbt);
	}

	@Override
	public NbtCompound toClientTag(NbtCompound nbt) {
		return writeCommonNbt(nbt);
	}

	@Override
	public @Nullable Object getRenderAttachmentData() {
		return Pair.of(base, extra);
	}
}
