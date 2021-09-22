package dev.micalobia.full_slabs.mixin.world;

import com.mojang.datafixers.util.Pair;
import dev.micalobia.full_slabs.FullSlabsMod;
import dev.micalobia.full_slabs.block.entity.FullSlabBlockEntity;
import dev.micalobia.full_slabs.util.Utility;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldChunk.class)
public abstract class WorldChunkMixin {

	@Redirect(method = "setBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockEntityProvider;createBlockEntity(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Lnet/minecraft/block/entity/BlockEntity;"))
	public BlockEntity interceptFullSlabPlacement(BlockEntityProvider blockEntityProvider, BlockPos pos, BlockState state) {
		if(!state.isOf(FullSlabsMod.FULL_SLAB_BLOCK))
			return ((BlockEntityProvider) state.getBlock()).createBlockEntity(pos, state);
		Pair<Block, Block> pair = Utility.getGhostPair();
		return new FullSlabBlockEntity(pos, state, pair.getFirst(), pair.getSecond());
	}
}
