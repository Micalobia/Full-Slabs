package dev.micalobia.full_slabs.event;

import dev.micalobia.full_slabs.block.Blocks;
import dev.micalobia.full_slabs.block.FullSlabBlock;
import dev.micalobia.full_slabs.block.VerticalSlabBlock;
import dev.micalobia.full_slabs.block.entity.FullSlabBlockEntity;
import dev.micalobia.full_slabs.block.enums.SlabState;
import dev.micalobia.full_slabs.util.Helper;
import dev.micalobia.micalibria.event.ClientPlayerBrokeBlockEvent;
import dev.micalobia.micalibria.event.HitGroundParticlesEvent;
import dev.micalobia.micalibria.event.ServerPlayerBrokeBlockEvent;
import dev.micalobia.micalibria.event.SpawnSprintingParticlesEvent;
import dev.micalobia.micalibria.event.enums.EventReaction;
import dev.micalobia.micalibria.event.enums.TypedEventReaction;
import dev.micalobia.micalibria.server.network.ServerPlayerEntityUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class Events {
	static {
		SpawnSprintingParticlesEvent.EVENT.register(Events::spawnSlabSprintingParticles);
		HitGroundParticlesEvent.EVENT.register(Events::spawnHitGroundParticles);
		ServerPlayerBrokeBlockEvent.EVENT.register(Events::tryBreakFullSlab);
		ServerPlayerBrokeBlockEvent.EVENT.register(Events::tryBreakHorizontalSlab);
		ServerPlayerBrokeBlockEvent.EVENT.register(Events::tryBreakVerticalSlab);
		ClientPlayerBrokeBlockEvent.EVENT.register(Events::breakFullSlab);
		ClientPlayerBrokeBlockEvent.EVENT.register(Events::breakHorizontalSlab);
		ClientPlayerBrokeBlockEvent.EVENT.register(Events::breakVerticalSlab);
	}

	private static EventReaction breakVerticalSlab(MinecraftClient client, BlockPos pos) {
		BlockState state = client.world.getBlockState(pos);
		Block block = state.getBlock();
		if(!(block instanceof VerticalSlabBlock)) return EventReaction.IGNORE;
		if(state.get(VerticalSlabBlock.STATE) != SlabState.DOUBLE) return EventReaction.IGNORE;
		Axis axis = state.get(VerticalSlabBlock.AXIS);
		Vec3d hit = client.crosshairTarget.getPos();
		boolean positive = Helper.isPositive(hit, pos, axis);
		BlockState brokenState = Helper.getState(block, axis, positive);
		BlockState leftoverState = Helper.getState(block, axis, !positive);
		boolean ret = breakSlab(brokenState, leftoverState, pos, client);
		return ret ? EventReaction.COMPLETE : EventReaction.CANCEL; // TODO: Replace with EventReaction.terminate(bool)
	}

	private static EventReaction breakHorizontalSlab(MinecraftClient client, BlockPos pos) {
		BlockState state = client.world.getBlockState(pos);
		if(!(state.getBlock() instanceof SlabBlock)) return EventReaction.IGNORE;
		if(state.get(SlabBlock.TYPE) != SlabType.DOUBLE) return EventReaction.IGNORE;
		Vec3d hit = client.crosshairTarget.getPos();
		boolean positive = Helper.isPositive(hit, pos, Axis.Y);
		BlockState brokenState = state.with(SlabBlock.TYPE, positive ? SlabType.TOP : SlabType.BOTTOM);
		BlockState leftoverState = state.with(SlabBlock.TYPE, positive ? SlabType.BOTTOM : SlabType.TOP);
		boolean ret = breakSlab(brokenState, leftoverState, pos, client);
		return ret ? EventReaction.COMPLETE : EventReaction.CANCEL; // TODO: Replace with EventReaction.terminate(bool)
	}

	private static EventReaction breakFullSlab(MinecraftClient client, BlockPos pos) {
		ClientWorld world = client.world;
		BlockState state = world.getBlockState(pos);
		if(!state.isOf(Blocks.FULL_SLAB_BLOCK)) return EventReaction.IGNORE;
		FullSlabBlockEntity entity = (FullSlabBlockEntity) world.getBlockEntity(pos);
		if(entity == null) return EventReaction.CANCEL;
		Axis axis = state.get(FullSlabBlock.AXIS);
		Vec3d hit = client.crosshairTarget.getPos();
		boolean positive = Helper.isPositive(hit, pos, axis);
		boolean ret = breakSlab(entity.getState(axis, positive), entity.getState(axis, !positive), pos, client);
		return ret ? EventReaction.COMPLETE : EventReaction.CANCEL; // TODO: Replace with EventReaction.terminate(bool)
	}

	private static EventReaction tryBreakVerticalSlab(ServerWorld world, ServerPlayerEntity player, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		if(!(state.getBlock() instanceof VerticalSlabBlock)) return EventReaction.IGNORE;
		if(state.get(VerticalSlabBlock.STATE) != SlabState.DOUBLE) return EventReaction.IGNORE;

		HitResult hitResult = ServerPlayerEntityUtil.crosshair(player);
		if(hitResult.getType() != Type.BLOCK) return EventReaction.CANCEL;

		Vec3d hit = hitResult.getPos();
		Axis axis = state.get(VerticalSlabBlock.AXIS);
		boolean positive = Helper.isPositive(hit, pos, axis);
		Block block = state.getBlock();
		BlockState brokenState = Helper.getState(block, axis, positive);
		BlockState leftoverState = Helper.getState(block, axis, !positive);
		breakSlab(brokenState, leftoverState, pos, world, player);

		return EventReaction.COMPLETE;
	}

	private static EventReaction tryBreakHorizontalSlab(ServerWorld world, ServerPlayerEntity player, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		if(!(state.getBlock() instanceof SlabBlock)) return EventReaction.IGNORE;
		if(state.get(SlabBlock.TYPE) != SlabType.DOUBLE) return EventReaction.IGNORE;

		HitResult hitResult = ServerPlayerEntityUtil.crosshair(player);
		if(hitResult.getType() != Type.BLOCK) return EventReaction.CANCEL;

		Vec3d hit = hitResult.getPos();
		boolean positive = Helper.isPositive(hit, pos, Axis.Y);
		BlockState brokenState = state.with(SlabBlock.TYPE, positive ? SlabType.TOP : SlabType.BOTTOM);
		BlockState leftoverState = state.with(SlabBlock.TYPE, positive ? SlabType.BOTTOM : SlabType.TOP);
		breakSlab(brokenState, leftoverState, pos, world, player);

		return EventReaction.COMPLETE;
	}

	private static EventReaction tryBreakFullSlab(ServerWorld world, ServerPlayerEntity player, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		if(!state.isOf(Blocks.FULL_SLAB_BLOCK)) return EventReaction.IGNORE;

		FullSlabBlockEntity entity = (FullSlabBlockEntity) world.getBlockEntity(pos);
		if(entity == null) return EventReaction.CANCEL;

		HitResult hitResult = ServerPlayerEntityUtil.crosshair(player);
		if(hitResult.getType() != Type.BLOCK) return EventReaction.CANCEL;

		Vec3d hit = hitResult.getPos();
		Axis axis = state.get(FullSlabBlock.AXIS);
		boolean positive = Helper.isPositive(hit, pos, axis);
		breakSlab(entity.getState(axis, positive), entity.getState(axis, !positive), pos, world, player);

		return EventReaction.COMPLETE;
	}

	private static void breakSlab(BlockState brokenState, BlockState leftoverState, BlockPos pos, ServerWorld world, ServerPlayerEntity player) {
		Block broken = brokenState.getBlock();
		broken.onBreak(world, pos, brokenState, player);
		boolean changed = world.setBlockState(pos, leftoverState, 3);
		if(changed) broken.onBroken(world, pos, brokenState);
		if(!player.isCreative()) {
			ItemStack hand = player.getMainHandStack();
			ItemStack handCopy = hand.copy();
			boolean effectiveTool = player.isUsingEffectiveTool(brokenState);
			hand.postMine(world, brokenState, pos, player);
			if(changed && effectiveTool)
				broken.afterBreak(world, player, pos, brokenState, null, handCopy);
		}
	}

	private static boolean breakSlab(BlockState brokenState, BlockState leftoverState, BlockPos pos, MinecraftClient client) {
		Block broken = brokenState.getBlock();
		broken.onBreak(client.world, pos, brokenState, client.player);
		boolean changed = client.world.setBlockState(pos, leftoverState, 11);
		if(changed) broken.onBroken(client.world, pos, brokenState);
		return changed;
	}

	public static void init() {
	}

	private static TypedEventReaction<BlockState> spawnHitGroundParticles(BlockState blockState, LivingEntity entity) {
		if(!blockState.isOf(Blocks.FULL_SLAB_BLOCK)) return TypedEventReaction.ignore();
		BlockPos pos = new BlockPos(MathHelper.floor(entity.getX()), MathHelper.ceil(entity.getY() - 1), MathHelper.floor(entity.getZ()));
		FullSlabBlockEntity blockEntity = (FullSlabBlockEntity) entity.world.getBlockEntity(pos);
		if(blockEntity == null) return TypedEventReaction.cancel();
		Axis axis = blockState.get(FullSlabBlock.AXIS);
		Vec3d hit = new Vec3d(entity.getX(), entity.getY(), entity.getZ());
		return TypedEventReaction.complete(blockEntity.getHitState(axis, hit));
	}

	private static TypedEventReaction<BlockState> spawnSlabSprintingParticles(BlockState blockState, BlockPos blockPos, Entity entity) {
		if(!blockState.isOf(Blocks.FULL_SLAB_BLOCK)) return TypedEventReaction.ignore();
		FullSlabBlockEntity blockEntity = (FullSlabBlockEntity) entity.world.getBlockEntity(blockPos);
		if(blockEntity == null) return TypedEventReaction.cancel();
		Vec3d position = entity.getPos().subtract(0d, 0.20000000298023224d, 0d);
		Axis axis = blockState.get(FullSlabBlock.AXIS);
		return TypedEventReaction.complete(blockEntity.getHitState(axis, position));
	}


}
