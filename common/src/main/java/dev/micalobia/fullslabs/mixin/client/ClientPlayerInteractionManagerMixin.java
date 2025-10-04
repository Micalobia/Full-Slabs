package dev.micalobia.fullslabs.mixin.client;

import dev.micalobia.fullslabs.util.Utility;
import dev.micalobia.fullslabs.util.Utility.StatePair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {

    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "breakBlock", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;onBreak(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/block/BlockState;"))
    private void interceptSlabBreak(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        var world = Objects.requireNonNull(client.world);
        var state = world.getBlockState(pos);
        if (Utility.isDoubleSlab(state)) {
            var pair = Utility.breakHalf(state, pos, client.crosshairTarget);
            // Pair is only null if state isn't a double slab, which we check before calling
            assert pair != null;
            var ret = fullslabs$breakSlab(pair, pos);
            cir.setReturnValue(ret);
        }
    }

    @Unique
    private boolean fullslabs$breakSlab(StatePair pair, BlockPos pos) {
        var broken = pair.left().getBlock();
        var world = Objects.requireNonNull(client.world);
        broken.onBreak(Objects.requireNonNull(world), pos, pair.left(), client.player);
        var changed = world.setBlockState(pos, pair.right(), 11);
        if (changed) broken.onBroken(world, pos, pair.left());
        return changed;
    }
}
