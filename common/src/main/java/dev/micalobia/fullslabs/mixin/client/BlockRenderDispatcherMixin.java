package dev.micalobia.fullslabs.mixin.client;

import dev.micalobia.fullslabs.util.Utility;
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
    private BlockState changeSlabDamageRender(BlockState state, BlockState ignored, BlockPos pos, BlockAndTintGetter view) {
        var hit = Minecraft.getInstance().hitResult;
        if (!(hit instanceof BlockHitResult)) return state;
        return Utility.targetedHalf(view, state, pos, hit.getLocation());
    }
}
