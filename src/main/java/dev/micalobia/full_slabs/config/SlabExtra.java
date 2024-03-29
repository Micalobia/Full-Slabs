package dev.micalobia.full_slabs.config;

import com.google.gson.*;
import dev.micalobia.full_slabs.util.Utility;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SlabExtra {
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
			@Nullable String bottom, @Nullable String top,
			@Nullable String north, @Nullable String south,
			@Nullable String east, @Nullable String west
	) {
		this(block,
				Utility.getStateFromString(block, bottom),
				Utility.getStateFromString(block, top),
				Utility.getStateFromString(block, north),
				Utility.getStateFromString(block, south),
				Utility.getStateFromString(block, east),
				Utility.getStateFromString(block, west)
		);
	}

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
