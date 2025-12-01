package dev.micalobia.fullslabs.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.micalobia.fullslabs.ducks.ServerPlayerInteractionManagerDuck;
import dev.micalobia.fullslabs.util.Utility;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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

    @Inject(method = "destroyBlock", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;playerWillDestroy(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private void interceptSlabBreaking(BlockPos pos, CallbackInfoReturnable<Boolean> cir, @Local BlockState state) {
        var crosshair = Utility.crosshair(this.player);
        if (crosshair.getType() != HitResult.Type.BLOCK) return;
        var pair = Utility.breakHalf(this.level, state, pos, crosshair);
        if (pair == null) return;
        fullslabs$breakSlab(pair, pos);
        cir.setReturnValue(true);
    }

    @Unique
    private void fullslabs$breakSlab(Utility.StatePair pair, BlockPos pos) {
        var broken = pair.towards().getBlock();
        broken.playerWillDestroy(this.level, pos, pair.towards(), this.player);
        var changed = this.level.setBlock(pos, pair.away(), 3);
        if (changed) broken.destroy(this.level, pos, pair.towards());
        if (!this.player.isCreative()) {
            var hand = this.player.getMainHandItem();
            var handCopy = hand.copy();
            var effectiveTool = this.player.hasCorrectToolForDrops(pair.towards());
            hand.mineBlock(this.level, pair.towards(), pos, this.player);
            if (changed && effectiveTool)
                broken.playerDestroy(this.level, this.player, pos, pair.towards(), null, handCopy);
            if (hand.isEmpty() && !handCopy.isEmpty())
                fullslabs$onPlayerDestroyItem(this.player, handCopy, InteractionHand.MAIN_HAND);
        }
    }
}
