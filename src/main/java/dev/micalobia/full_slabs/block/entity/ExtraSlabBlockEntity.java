package dev.micalobia.full_slabs.block.entity;

import dev.micalobia.full_slabs.FullSlabsMod;
import dev.micalobia.full_slabs.block.ExtraSlabBlock;
import dev.micalobia.full_slabs.block.SlabBlockUtility;
import dev.micalobia.full_slabs.config.ModConfig;
import dev.micalobia.full_slabs.config.SlabExtra;
import dev.micalobia.full_slabs.mixin.item.WallStandingBlockItemAccessor;
import dev.micalobia.full_slabs.util.Utility;
import dev.micalobia.micalibria.block.entity.MBlockEntity;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.block.*;
import net.minecraft.block.enums.SlabType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.WallStandingBlockItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public class ExtraSlabBlockEntity extends MBlockEntity {
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
		SlabType type = state.get(SlabBlock.TYPE);
		if(type == SlabType.DOUBLE) return false;
		Axis axis = state.get(Properties.AXIS);
		SlabExtra ret = get(axis, extra);
		if(ret == null) return false;
		Direction direction = SlabBlockUtility.getDirection(type, axis);
		return ret.allowed(direction);
	}

	public static Optional<BlockState> getExtra(BlockState state, BlockItem item) {
		if(!(state.getBlock() instanceof SlabBlock)) return Optional.empty();
		SlabType type = state.get(SlabBlock.TYPE);
		if(type == SlabType.DOUBLE) return Optional.empty();
		Axis axis = state.get(Properties.AXIS);
		SlabExtra retExtra = get(axis, item);
		if(retExtra == null) return Optional.empty();
		Direction direction = SlabBlockUtility.getDirection(type, axis);
		BlockState retState = retExtra.getState(direction);
		return Optional.ofNullable(retState);
	}

	public static @Nullable SlabExtra get(Block block) {
		return allowedExtras.get(Utility.getBlockId(block));
	}

	public static @Nullable SlabExtra get(Axis axis, BlockItem item) {
		if(axis.isHorizontal() && item instanceof WallStandingBlockItem wallItem)
			return get(((WallStandingBlockItemAccessor) wallItem).getWallBlock());
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
		return getExtra().getState(SlabBlockUtility.getDirection(type, axis));
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

	public BlockState getState(Vec3d hit) {
		Axis axis = getCachedState().get(ExtraSlabBlock.AXIS);
		SlabType type = getCachedState().get(ExtraSlabBlock.TYPE);
		boolean positive = SlabBlockUtility.isPositive(axis, hit, pos, type);
		return positive == (type == SlabType.TOP) ? getBaseState() : getExtraState();
	}

	public BlockState getOppositeState(Vec3d hit) {
		Axis axis = getCachedState().get(ExtraSlabBlock.AXIS);
		SlabType type = getCachedState().get(ExtraSlabBlock.TYPE);
		boolean positive = SlabBlockUtility.isPositive(axis, hit, pos, type);
		return positive != (type == SlabType.TOP) ? getBaseState() : getExtraState();
	}

	protected Block getBase() {
		return this.base;
	}

	public boolean waterloggable() {
		return this.getExtra().waterloggable();
	}

	@Override
	public void writeToNbt(NbtCompound nbt) {
		nbt.putString("base_identifier", Utility.getBlockId(this.base).toString());
		nbt.putString("extra_identifier", Utility.getBlockId(this.extra.getBlock()).toString());
	}

	@Override
	public void readFromNbt(NbtCompound nbt) {
		this.base = Utility.getBlock(new Identifier(nbt.getString("base_identifier")));
		if(!(this.base instanceof SlabBlock))
			this.base = Blocks.SMOOTH_STONE_SLAB;
		Identifier extra = new Identifier(nbt.getString("extra_identifier"));
		this.extra = allowedExtras.getOrDefault(extra, null);
	}

	@Override
	public void writeToClientNbt(NbtCompound nbt) {
		nbt.putString("base_identifier", Utility.getBlockId(this.base).toString());
		nbt.putString("extra_identifier", Utility.getBlockId(this.extra.getBlock()).toString());
	}

	@Override
	public void readFromClientNbt(NbtCompound nbt) {
		this.base = Utility.getBlock(new Identifier(nbt.getString("base_identifier")));
		if(!(this.base instanceof SlabBlock))
			this.base = Blocks.SMOOTH_STONE_SLAB;
		Identifier extra = new Identifier(nbt.getString("extra_identifier"));
		this.extra = allowedExtras.getOrDefault(extra, null);
	}
}
