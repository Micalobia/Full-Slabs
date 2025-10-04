package dev.micalobia.fullslabs.mixin.client;

import dev.micalobia.fullslabs.block.VerticalSlabBlock;
import dev.micalobia.fullslabs.block.VerticalSlabBlock.VerticalType;
import dev.micalobia.fullslabs.util.Utility;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(BlockRenderManager.class)
public class BlockRenderManagerMixin {
    @ModifyArg(method = "renderDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/BlockModels;getModel(Lnet/minecraft/block/BlockState;)Lnet/minecraft/client/render/model/BlockStateModel;"))
    private BlockState changSlabDamageRender(BlockState state) {
        if (!Utility.isDoubleSlab(state)) return state;
        var hitResult = MinecraftClient.getInstance().crosshairTarget;
        if (!(hitResult instanceof BlockHitResult bhr)) return state;
        var hit = bhr.getPos();
        var pos = bhr.getBlockPos();
        var block = state.getBlock();
        if (block instanceof SlabBlock) {
            var target = Utility.getAxisTargetDirection(hit, pos, Axis.Y);
            var top = target == Direction.UP;
            return state.with(Properties.SLAB_TYPE, top ? SlabType.TOP : SlabType.BOTTOM);
        }
        if (block instanceof VerticalSlabBlock) {
            var facing = state.get(Properties.HORIZONTAL_FACING);
            var target = Utility.getAxisTargetDirection(hit, pos, facing.getAxis());
            var towards = facing == target;
            return state.with(VerticalSlabBlock.TYPE, towards ? VerticalType.TOWARDS : VerticalType.AWAY);
        }
        return state;
    }
}
