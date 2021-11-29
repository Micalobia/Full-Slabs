package dev.micalobia.full_slabs.client.render.model;

import com.mojang.datafixers.util.Pair;
import dev.micalobia.full_slabs.block.FullSlabBlock;
import dev.micalobia.full_slabs.block.SlabBlockUtility;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.BlockRenderView;

import java.util.Random;
import java.util.function.Supplier;

public class FullSlabModel extends BasicModel {
	@SuppressWarnings("unchecked")
	@Override
	public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
		RenderAttachedBlockView view = (RenderAttachedBlockView) blockView;
		Pair<Block, Block> pair = (Pair<Block, Block>) view.getBlockEntityRenderAttachment(pos);
		Axis axis = state.get(FullSlabBlock.AXIS);
		BlockState positiveState = SlabBlockUtility.getSlabState(pair, axis, true);
		BlockState negativeState = SlabBlockUtility.getSlabState(pair, axis, false);
		emitModel(view, positiveState, pos, randomSupplier, context);
		emitModel(view, negativeState, pos, randomSupplier, context);
	}
}
