package dev.micalobia.full_slabs.mixin.world;

import com.mojang.datafixers.util.Pair;
import dev.micalobia.full_slabs.block.entity.FullSlabBlockEntity;
import dev.micalobia.full_slabs.util.Utility;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(WorldChunk.class)
public abstract class WorldChunkMixin {
	@ModifyVariable(method = "setBlockState", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/block/BlockEntityProvider;createBlockEntity(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Lnet/minecraft/block/entity/BlockEntity;"))
	private BlockEntity changeFullSlabEntity(BlockEntity entity) {
		if(entity instanceof FullSlabBlockEntity) {
			Pair<Block, Block> pair = Utility.getGhostPair();
			return new FullSlabBlockEntity(entity.getPos(), entity.getCachedState(), pair.getFirst(), pair.getSecond());
		} else return entity;
	}
}
