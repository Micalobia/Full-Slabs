package dev.micalobia.fullslabs.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.micalobia.fullslabs.util.Utility;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {

    @Shadow
    protected ServerWorld world;

    @Shadow
    @Final
    protected ServerPlayerEntity player;

    @Inject(method = "tryBreakBlock", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;onBreak(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/block/BlockState;"))
    private void interceptSlabBreaking(BlockPos pos, CallbackInfoReturnable<Boolean> cir, @Local BlockState state) {
        if (Utility.isDoubleSlab(state)) {
            var crosshair = Utility.crosshair(this.player);
            if (!(crosshair instanceof BlockHitResult)) {
                cir.setReturnValue(false);
                return;
            }
            var pair = Utility.breakHalf(state, pos, crosshair);
            // Pair is only null if state isn't a double slab, which we check before calling
            assert pair != null;
            fullslabs$breakSlab(pair, pos);
            cir.setReturnValue(true);
        }
    }

    @Unique
    private void fullslabs$breakSlab(Utility.StatePair pair, BlockPos pos) {
        var broken = pair.left().getBlock();
        broken.onBreak(this.world, pos, pair.left(), this.player);
        var changed = world.setBlockState(pos, pair.right(), 3);
        if (changed) broken.onBroken(this.world, pos, pair.left());
        if (!this.player.isCreative()) {
            var hand = this.player.getMainHandStack();
            var handCopy = hand.copy();
            var effectiveTool = this.player.canHarvest(pair.left());
            hand.postMine(this.world, pair.left(), pos, this.player);
            if (changed && effectiveTool)
                broken.afterBreak(this.world, this.player, pos, pair.left(), null, handCopy);
        }
    }
}
