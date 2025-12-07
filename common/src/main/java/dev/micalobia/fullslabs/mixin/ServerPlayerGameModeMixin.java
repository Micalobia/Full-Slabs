package dev.micalobia.fullslabs.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.micalobia.fullslabs.block.SlabLike;
import dev.micalobia.fullslabs.ducks.ServerPlayerInteractionManagerDuck;
import dev.micalobia.fullslabs.handlers.MixedHandlers;
import dev.micalobia.fullslabs.util.Utility;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeMixin implements ServerPlayerInteractionManagerDuck {

    @Shadow
    protected ServerLevel level;

    @Shadow
    @Final
    protected ServerPlayer player;

    @Shadow
    @Final
    private static Logger LOGGER;

    @Inject(method = "destroyBlock", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;playerWillDestroy(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private void interceptSlabBreaking(BlockPos pos, CallbackInfoReturnable<Boolean> cir, @Local BlockState state, @Local BlockEntity blockEntity) {
        var crosshair = Utility.crosshair(this.player);
        if (crosshair.getType() != HitResult.Type.BLOCK) return;
        if (!(state.getBlock() instanceof SlabLike slab) || slab.isSingle(state)) return;
        cir.setReturnValue(true);
        var breakTowards = slab.getType(state).isAxisTargetTowards(crosshair.getLocation(), pos);
        var brokenState = slab.getHalf(state, this.level, pos, breakTowards);
        var keptState = slab.getHalf(state, this.level, pos, !breakTowards);
        var broken = brokenState.getBlock();
        var changed = this.level.setBlock(pos, keptState, Block.UPDATE_ALL);
        broken.playerWillDestroy(this.level, pos, brokenState, this.player);
        if (SharedConstants.DEBUG_BLOCK_BREAK)
            LOGGER.info("server broke {} {} -> {}", pos, brokenState, this.level.getBlockState(pos));
        if (changed) broken.destroy(this.level, pos, brokenState);
        if (this.player.preventsBlockDrops()) return;
        var hand = this.player.getMainHandItem();
        var handCopy = hand.copy();
        var effectiveTool = this.player.hasCorrectToolForDrops(brokenState);
        hand.mineBlock(this.level, brokenState, pos, this.player);
        if (changed && effectiveTool) {
            MixedHandlers.tryGet(broken).ifPresentOrElse(
                    handler -> handler.playerDestroy(this.level, this.player, pos, brokenState, blockEntity, handCopy),
                    () -> broken.playerDestroy(this.level, this.player, pos, brokenState, blockEntity, handCopy)
            );
        }
        if (hand.isEmpty() && !handCopy.isEmpty())
            fullslabs$onPlayerDestroyItem(this.player, handCopy, InteractionHand.MAIN_HAND);
    }
}
