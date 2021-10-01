package dev.micalobia.full_slabs.client.render;

import dev.micalobia.full_slabs.FullSlabsMod;
import dev.micalobia.full_slabs.block.ExtraSlabBlock;
import dev.micalobia.full_slabs.block.FullSlabBlock;
import dev.micalobia.full_slabs.block.entity.ExtraSlabBlockEntity;
import dev.micalobia.full_slabs.mixin.client.render.WorldRendererAccessor;
import dev.micalobia.full_slabs.util.Utility;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext.BlockOutlineContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.state.property.Properties;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

import java.util.Objects;

public class OutlineRenderer {
	private static boolean renderSlabOutline(WorldRenderContext renderContext, BlockOutlineContext outlineContext) {
		if(outlineContext.blockState().getBlock() instanceof SlabBlock) {
			BlockState state = outlineContext.blockState();
			BlockPos pos = outlineContext.blockPos();
			Vec3d cam = renderContext.camera().getPos();
			WorldRendererAccessor.callDrawShapeOutline(
					renderContext.matrixStack(),
					Objects.requireNonNull(renderContext.consumers()).getBuffer(RenderLayer.LINES),
					getRenderedSlabOutlineShape(state, pos),
					pos.getX() - cam.getX(),
					pos.getY() - cam.getY(),
					pos.getZ() - cam.getZ(),
					0f, 0f, 0f, 0.4f
			);
			return false;
		}
		return true;
	}

	private static boolean renderFullSlabOutline(WorldRenderContext renderContext, BlockOutlineContext outlineContext) {
		if(outlineContext.blockState().isOf(FullSlabsMod.FULL_SLAB_BLOCK)) {
			BlockState state = outlineContext.blockState();
			BlockPos pos = outlineContext.blockPos();
			Vec3d cam = renderContext.camera().getPos();
			WorldRendererAccessor.callDrawShapeOutline(
					renderContext.matrixStack(),
					Objects.requireNonNull(renderContext.consumers()).getBuffer(RenderLayer.LINES),
					getRenderedFullSlabOutlineShape(state, pos),
					pos.getX() - cam.getX(),
					pos.getY() - cam.getY(),
					pos.getZ() - cam.getZ(),
					0f, 0f, 0f, 0.4f
			);
			return false;
		}
		return true;
	}

	private static boolean renderExtraSlabOutline(WorldRenderContext renderContext, BlockOutlineContext outlineContext) {
		if(outlineContext.blockState().isOf(FullSlabsMod.EXTRA_SLAB_BLOCK)) {
			BlockState state = outlineContext.blockState();
			BlockPos pos = outlineContext.blockPos();
			BlockView world = renderContext.world();
			Vec3d cam = renderContext.camera().getPos();
			WorldRendererAccessor.callDrawShapeOutline(
					renderContext.matrixStack(),
					Objects.requireNonNull(renderContext.consumers()).getBuffer(RenderLayer.LINES),
					getRenderedExtraSlabOutlineShape(state, pos, world),
					pos.getX() - cam.getX(),
					pos.getY() - cam.getY(),
					pos.getZ() - cam.getZ(),
					0f, 0f, 0f, 0.4f
			);
			return false;
		}
		return true;
	}

	private static VoxelShape getRenderedFullSlabOutlineShape(BlockState state, BlockPos pos) {
		HitResult hitResult = MinecraftClient.getInstance().crosshairTarget;
		if(hitResult == null) return VoxelShapes.fullCube();
		return Utility.getShape(Utility.getDirection(state.get(FullSlabBlock.AXIS), hitResult.getPos(), pos));
	}

	private static VoxelShape getRenderedSlabOutlineShape(BlockState state, BlockPos pos) {
		SlabType type = state.get(SlabBlock.TYPE);
		Direction direction;
		if(type == SlabType.DOUBLE) {
			HitResult hitResult = MinecraftClient.getInstance().crosshairTarget;
			if(hitResult == null) return VoxelShapes.fullCube();
			direction = Utility.getDirection(state.get(Properties.AXIS), hitResult.getPos(), pos);
		} else direction = Utility.getDirection(type, state.get(Properties.AXIS));
		return Utility.getShape(direction);
	}

	private static VoxelShape getRenderedExtraSlabOutlineShape(BlockState state, BlockPos pos, BlockView world) {
		HitResult hitResult = MinecraftClient.getInstance().crosshairTarget;
		if(hitResult == null) return VoxelShapes.fullCube();
		SlabType type = state.get(ExtraSlabBlock.TYPE);
		Axis axis = state.get(ExtraSlabBlock.AXIS);
		Direction slabDir = Utility.getDirection(type, axis);
		Direction hitDir = Utility.getDirection(axis, hitResult.getPos(), pos, type);
		if(slabDir == hitDir) return Utility.getShape(slabDir);
		ExtraSlabBlockEntity entity = (ExtraSlabBlockEntity) world.getBlockEntity(pos);
		if(entity == null) return Utility.getShape(hitDir);
		return entity.getExtraOutlineShape(world, pos, ShapeContext.absent());
	}

	public static void init() {
		WorldRenderEvents.BLOCK_OUTLINE.register(OutlineRenderer::renderSlabOutline);
		WorldRenderEvents.BLOCK_OUTLINE.register(OutlineRenderer::renderFullSlabOutline);
		WorldRenderEvents.BLOCK_OUTLINE.register(OutlineRenderer::renderExtraSlabOutline);
	}
}
