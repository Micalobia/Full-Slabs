package dev.micalobia.fullslabs.mixin.client;

import dev.micalobia.fullslabs.SlabRegistry;
import dev.micalobia.fullslabs.block.MixedSlabBlock.MixedType;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BlockRenderManager.class)
public class BlockRenderManagerMixin {
    @ModifyVariable(method = "renderDamage", at = @At("HEAD"), argsOnly = true)
    private BlockState changeSlabDamageRender(BlockState state, BlockState ignored, BlockPos pos, BlockRenderView view) {
        var mixed = SlabRegistry.MIXED_SLAB.get();
        if (!(Utility.isDoubleSlab(state) || state.isOf(mixed))) return state;
        var hitResult = MinecraftClient.getInstance().crosshairTarget;
        if (!(hitResult instanceof BlockHitResult bhr)) return state;
        var hit = bhr.getPos();
        var block = state.getBlock();
        var towards = MixedType.fromState(state).isAxisTargetTowards(hit, pos);
        if (block instanceof SlabBlock)
            return state.with(Properties.SLAB_TYPE, towards ? SlabType.TOP : SlabType.BOTTOM);
        if (block instanceof VerticalSlabBlock)
            return state.with(VerticalSlabBlock.TYPE, towards ? VerticalType.TOWARDS : VerticalType.AWAY);
        if (block == mixed) return mixed.act(view, pos, entity -> {
            return entity.getTargetedState(bhr);
        });
        return state;
    }
}
