package dev.micalobia.fullslabs.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import dev.micalobia.fullslabs.SlabRegistry;
import dev.micalobia.fullslabs.block.MixedSlabBlock;
import dev.micalobia.fullslabs.block.MixedSlabBlock.MixedType;
import dev.micalobia.fullslabs.block.VerticalSlabBlock;
import dev.micalobia.fullslabs.util.Utility;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.state.property.Properties;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
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
        var mixed = SlabRegistry.MIXED_SLAB.get();
        if (!(Utility.isDoubleSlab(state) || state.isOf(mixed))) return state;
        var crosshair = Objects.requireNonNull(client.crosshairTarget);
        if (!(crosshair instanceof BlockHitResult bhr)) return state;
        var block = state.getBlock();
        var towards = MixedType.fromState(state).isAxisTargetTowards(crosshair.getPos(), pos);
        if (block instanceof SlabBlock)
            return state.with(Properties.SLAB_TYPE, towards ? SlabType.TOP : SlabType.BOTTOM);
        if (block instanceof VerticalSlabBlock)
            return state.with(VerticalSlabBlock.TYPE, towards ? VerticalSlabBlock.VerticalType.TOWARDS : VerticalSlabBlock.VerticalType.AWAY);
        if (block instanceof MixedSlabBlock) return mixed.act(view, pos, entity -> {
            return entity.getTargetedState(bhr);
        });
        return state;
    }
}
