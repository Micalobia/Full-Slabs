package dev.micalobia.fullslabs.mixin.client;

import dev.micalobia.fullslabs.block.SlabLike;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BlockRenderDispatcher.class)
public class BlockRenderDispatcherMixin {
    @ModifyVariable(method = "renderBreakingTexture", at = @At("HEAD"), argsOnly = true)
    private BlockState changeSlabDamageRender(BlockState state, BlockState ignored, BlockPos pos, BlockAndTintGetter level) {
        var crosshair = Minecraft.getInstance().hitResult;
        if (!(crosshair instanceof BlockHitResult)) return state;
        if (!(state.getBlock() instanceof SlabLike slab) || slab.isSingle(state)) return state;
        return slab.getHalf(state, level, pos, slab.isHitTowards(state, pos, crosshair));
    }
}
