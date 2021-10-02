package dev.micalobia.full_slabs.block.entity;

import com.google.gson.*;
import com.mojang.datafixers.util.Pair;
import dev.micalobia.full_slabs.FullSlabsMod;
import dev.micalobia.full_slabs.block.ExtraSlabBlock;
import dev.micalobia.full_slabs.mixin.item.WallStandingBlockItemAccessor;
import dev.micalobia.full_slabs.util.Utility;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.SlabType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.WallStandingBlockItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtraSlabBlockEntity extends BlockEntity implements BlockEntityClientSerializable, RenderAttachmentBlockEntity {
	public static final Map<Identifier, SlabExtra> allowedExtras;

	static {
		allowedExtras = new HashMap<>();
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

	public static class SlabExtra {
		private final Block block;
		@Nullable
		private final BlockState bottomState;
		@Nullable
		private final BlockState topState;
		@Nullable
		private final BlockState northState;
		@Nullable
		private final BlockState southState;
		@Nullable
		private final BlockState eastState;
		@Nullable
		private final BlockState westState;

		public SlabExtra(
				Block block,
				@Nullable BlockState bottom, @Nullable BlockState top,
				@Nullable BlockState north, @Nullable BlockState south,
				@Nullable BlockState east, @Nullable BlockState west
		) {
			this.block = block;
			Identifier id = Utility.getBlockId(block);
			if(bottom != null && !bottom.isOf(block))
				throw new IllegalArgumentException(String.format("Argument 'bottom' is not of '%s'; Is of '%s'", id, Utility.getBlockId(bottom.getBlock())));
			if(top != null && !top.isOf(block))
				throw new IllegalArgumentException(String.format("Argument 'top' is not of '%s'; Is of '%s'", id, Utility.getBlockId(top.getBlock())));
			if(north != null && !north.isOf(block))
				throw new IllegalArgumentException(String.format("Argument 'north' is not of '%s'; Is of '%s'", id, Utility.getBlockId(north.getBlock())));
			if(south != null && !south.isOf(block))
				throw new IllegalArgumentException(String.format("Argument 'south' is not of '%s'; Is of '%s'", id, Utility.getBlockId(south.getBlock())));
			if(east != null && !east.isOf(block))
				throw new IllegalArgumentException(String.format("Argument 'east' is not of '%s'; Is of '%s'", id, Utility.getBlockId(east.getBlock())));
			if(west != null && !west.isOf(block))
				throw new IllegalArgumentException(String.format("Argument 'west' is not of '%s'; Is of '%s'", id, Utility.getBlockId(west.getBlock())));
			this.bottomState = bottom;
			this.topState = top;
			this.northState = north;
			this.southState = south;
			this.eastState = east;
			this.westState = west;
		}

		public Block getBlock() {
			return this.block;
		}

		public boolean waterloggable() {
			return block instanceof Waterloggable;
		}

		private VoxelShape getShape(Direction direction, BlockView world, BlockPos pos, ShapeContext context, FGetShape supplier) {
			BlockState state = getState(direction);
			if(state == null) return VoxelShapes.empty();
			VoxelShape shape = supplier.get(state, world, pos, context);
			return switch(direction) {
				case DOWN -> shape.offset(0d, 0.5d, 0d);
				case UP -> shape.offset(0d, -0.5d, 0d);
				case NORTH -> shape.offset(0d, 0d, 0.5d);
				case SOUTH -> shape.offset(0d, 0d, -0.5d);
				case WEST -> shape.offset(0.5d, 0d, 0d);
				case EAST -> shape.offset(-0.5d, 0d, 0d);
			};
		}

		public VoxelShape getOutlineShape(Direction direction, BlockView world, BlockPos pos, ShapeContext context) {
			return getShape(direction, world, pos, context, this.block::getOutlineShape);
		}

		public VoxelShape getCollisionShape(Direction direction, BlockView world, BlockPos pos, ShapeContext context) {
			return getShape(direction, world, pos, context, this.block::getCollisionShape);
		}

		public @Nullable BlockState getState(Direction direction) {
			return switch(direction) {
				case DOWN -> bottomState;
				case UP -> topState;
				case NORTH -> northState;
				case SOUTH -> southState;
				case WEST -> westState;
				case EAST -> eastState;
			};
		}

		public boolean allowed(Direction direction) {
			return switch(direction) {
				case DOWN -> bottomState != null;
				case UP -> topState != null;
				case NORTH -> northState != null;
				case SOUTH -> southState != null;
				case WEST -> westState != null;
				case EAST -> eastState != null;
			};
		}

		@FunctionalInterface
		private interface FGetShape {
			VoxelShape get(BlockState state, BlockView world, BlockPos pos, ShapeContext context);
		}

		public static class Deserializer implements JsonDeserializer<SlabExtra> {
			@Override
			public SlabExtra deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
				try {
					if(json.isJsonObject()) {
						JsonObject object = json.getAsJsonObject();
						Block block = Utility.getBlock(new Identifier(object.get("block").getAsString()));
						BlockState bottom = getState(block, "bottom", object);
						BlockState top = getState(block, "top", object);
						BlockState north = getState(block, "north", object);
						BlockState south = getState(block, "south", object);
						BlockState east = getState(block, "east", object);
						BlockState west = getState(block, "west", object);
						return new SlabExtra(block, bottom, top, north, south, east, west);
					} else throw new RuntimeException("Nope"); // TODO: Need to have real error handling
				} catch(Exception e) {
					e.printStackTrace();
					throw e;
				}
			}

			private @Nullable BlockState getState(Block block, String key, JsonObject object) {
				return object.has(key) ?
						stateFromString(block, object.get(key).getAsString()) :
						null;
			}

			private @Nullable BlockState stateFromString(Block block, String str) {
				if(str == null) return null;
				BlockState ret = block.getDefaultState();
				for(Property<?> property : block.getStateManager().getProperties()) {
					Pattern pattern = Pattern.compile(property.getName() + "=(?<value>\\w+)");
					Matcher matcher = pattern.matcher(str);
					if(!matcher.matches()) continue;
					String value = matcher.group("value");
					Optional<BlockState> with = with(ret, property, value);
					if(with.isPresent()) ret = with.get();
					else throw new IllegalArgumentException("word");
				}
				return ret;
			}

			private <T extends Comparable<T>> Optional<BlockState> with(BlockState state, Property<T> property, String name) {
				return property.parse(name).map(t -> state.with(property, t));
			}
		}
	}
}
