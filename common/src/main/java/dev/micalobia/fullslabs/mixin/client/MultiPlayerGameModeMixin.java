package dev.micalobia.fullslabs.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import dev.micalobia.fullslabs.block.SlabLike;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    @Final
    private static Logger LOGGER;

    @Inject(method = "destroyBlock", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;playerWillDestroy(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private void interceptSlabBreak(BlockPos pos, CallbackInfoReturnable<Boolean> cir, @Local BlockState state, @Local Level level) {
        if (!(state.getBlock() instanceof SlabLike slab) || slab.isSingle(state)) return;
        var crosshair = Objects.requireNonNull(this.minecraft.hitResult);
        var breakTowards = slab.getType(state).isAxisTargetTowards(crosshair.getLocation(), pos);
        var brokenState = slab.getHalf(state, level, pos, breakTowards);
        var keptState = slab.getHalf(state, level, pos, !breakTowards);
        var broken = brokenState.getBlock();
        broken.playerWillDestroy(level, pos, brokenState, Objects.requireNonNull(this.minecraft.player));
        var changed = level.setBlock(pos, keptState, Block.UPDATE_ALL_IMMEDIATE);
        if (changed) broken.destroy(level, pos, brokenState);
        if (SharedConstants.DEBUG_BLOCK_BREAK)
            LOGGER.error("client broke {} {} -> {}", pos, brokenState, level.getBlockState(pos));
        cir.setReturnValue(changed);
    }
}
