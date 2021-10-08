package dev.micalobia.full_slabs.client.render.model;

import com.mojang.datafixers.util.Pair;
import dev.micalobia.full_slabs.block.ExtraSlabBlock;
import dev.micalobia.full_slabs.config.SlabExtra;
import dev.micalobia.full_slabs.util.Utility;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.BlockRenderView;

import java.util.Random;
import java.util.function.Supplier;

public class ExtraSlabModel extends BasicModel {
	@SuppressWarnings("unchecked")
	@Override
	public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
		RenderAttachedBlockView view = (RenderAttachedBlockView) blockView;
		Pair<Block, SlabExtra> pair = (Pair<Block, SlabExtra>) view.getBlockEntityRenderAttachment(pos);
		assert pair != null;
		Block base = pair.getFirst();
		SlabExtra extra = pair.getSecond();
		SlabType type = state.get(ExtraSlabBlock.TYPE);
		Axis axis = state.get(ExtraSlabBlock.AXIS);
		Direction direction = Utility.getDirection(type, axis);
		BlockState baseState = base.getDefaultState().with(SlabBlock.TYPE, type).with(Properties.AXIS, axis);
		BlockState extraState = extra.getState(direction);
		BlockRenderManager manager = MinecraftClient.getInstance().getBlockRenderManager();
		context.fallbackConsumer().accept(manager.getModel(baseState));
		if(extraState != null) {
			Vec3f unit = direction.getOpposite().getUnitVector();
			unit.scale(0.5f); // Half normal
			context.pushTransform((quad) -> {
				for(int i = 0; i < 4; ++i) {
					Vec3f vec = quad.copyPos(i, null);
					vec.add(unit);
					quad.pos(i, vec);
				}
				return true;
			});
			FabricBakedModel model = (FabricBakedModel) manager.getModel(extraState);
			model.emitBlockQuads(blockView, state, pos, randomSupplier, context);
			context.popTransform();
		}
	}
}

