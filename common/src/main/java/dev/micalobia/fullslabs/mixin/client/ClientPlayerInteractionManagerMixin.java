package dev.micalobia.fullslabs.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import dev.micalobia.fullslabs.util.Utility;
import dev.micalobia.fullslabs.util.Utility.StatePair;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
    private void interceptSlabBreak(BlockPos pos, CallbackInfoReturnable<Boolean> cir, @Local BlockState state, @Local World world) {
        var pair = Utility.breakHalf(world, state, pos, this.client.crosshairTarget);
        if (pair == null) return;
        var ret = fullslabs$breakSlab(pair, pos, world);
        cir.setReturnValue(ret);
    }

    @Unique
    private boolean fullslabs$breakSlab(StatePair pair, BlockPos pos, World world) {
        var broken = pair.towards().getBlock();
        broken.onBreak(Objects.requireNonNull(world), pos, pair.towards(), client.player);
        var changed = world.setBlockState(pos, pair.away(), 11);
        if (changed) broken.onBroken(world, pos, pair.towards());
        return changed;
    }
}
