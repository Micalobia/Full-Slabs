package dev.micalobia.full_slabs.mixin.world;

import com.mojang.datafixers.util.Pair;
import dev.micalobia.full_slabs.block.SlabBlockUtility;
import dev.micalobia.full_slabs.block.entity.ExtraSlabBlockEntity;
import dev.micalobia.full_slabs.block.entity.FullSlabBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.world.chunk.WorldChunk;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(WorldChunk.class)
public abstract class WorldChunkMixin {
	@Shadow
	public abstract void setBlockEntity(BlockEntity blockEntity);

	// For compatibility with Carpet, doesn't make them movable but does prevent them from defaulting to STONE-STONE
	@ModifyVariable(method = "setBlockState", at = @At(shift = Shift.BEFORE, value = "JUMP", opcode = Opcodes.IFNONNULL))
	private BlockEntity changeFullSlabBlockEntityCarpet(BlockEntity entity) {
		BlockEntity blockEntity;
		if(entity instanceof FullSlabBlockEntity) {
			Pair<Block, Block> pair = SlabBlockUtility.getFullSlabGhost();
			blockEntity = new FullSlabBlockEntity(entity.getPos(), entity.getCachedState(), pair.getFirst(), pair.getSecond());
		} else if(entity instanceof ExtraSlabBlockEntity) {
			Pair<Block, BlockItem> pair = SlabBlockUtility.getExtraSlabGhost();
			blockEntity = new ExtraSlabBlockEntity(entity.getPos(), entity.getCachedState(), pair.getFirst(), pair.getSecond());
		} else return entity;
		this.setBlockEntity(blockEntity);
		return blockEntity;
	}

	@ModifyVariable(method = "setBlockState", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/block/BlockEntityProvider;createBlockEntity(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Lnet/minecraft/block/entity/BlockEntity;"))
	private BlockEntity changeFullSlabEntity(BlockEntity entity) {
		if(entity instanceof FullSlabBlockEntity) {
			Pair<Block, Block> pair = SlabBlockUtility.getFullSlabGhost();
			return new FullSlabBlockEntity(entity.getPos(), entity.getCachedState(), pair.getFirst(), pair.getSecond());
		} else if(entity instanceof ExtraSlabBlockEntity) {
			Pair<Block, BlockItem> pair = SlabBlockUtility.getExtraSlabGhost();
			return new ExtraSlabBlockEntity(entity.getPos(), entity.getCachedState(), pair.getFirst(), pair.getSecond());
		} else return entity;
	}
}
