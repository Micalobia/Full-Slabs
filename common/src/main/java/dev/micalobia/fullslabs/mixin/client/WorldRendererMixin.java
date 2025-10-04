package dev.micalobia.fullslabs.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import dev.micalobia.fullslabs.block.VerticalSlabBlock;
import dev.micalobia.fullslabs.util.Utility;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Objects;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @ModifyReceiver(method = "drawBlockOutline", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getOutlineShape(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/ShapeContext;)Lnet/minecraft/util/shape/VoxelShape;"))
    private BlockState changeRenderedOutline(BlockState state, BlockView view, BlockPos pos, ShapeContext ctx) {
        if (!Utility.isDoubleSlab(state)) return state;
        var hit = Objects.requireNonNull(client.crosshairTarget).getPos();
        var block = state.getBlock();
        if (block instanceof SlabBlock) {
            var target = Utility.getAxisTargetDirection(hit, pos, Direction.Axis.Y);
            var top = target == Direction.UP;
            return state.with(Properties.SLAB_TYPE, top ? SlabType.TOP : SlabType.BOTTOM);
        }
        if (block instanceof VerticalSlabBlock) {
            var facing = state.get(Properties.HORIZONTAL_FACING);
            var target = Utility.getAxisTargetDirection(hit, pos, facing.getAxis());
            var towards = facing == target;
            return state.with(VerticalSlabBlock.TYPE, towards ? VerticalSlabBlock.VerticalType.TOWARDS : VerticalSlabBlock.VerticalType.AWAY);
        }
        return state;
    }
}
