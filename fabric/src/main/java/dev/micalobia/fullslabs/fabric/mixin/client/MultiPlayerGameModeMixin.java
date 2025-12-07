package dev.micalobia.fullslabs.fabric.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.sugar.Local;
import dev.micalobia.fullslabs.block.SlabLike;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Objects;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @ModifyReceiver(method = "continueDestroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getSoundType()Lnet/minecraft/world/level/block/SoundType;"))
    private BlockState mixedSlabBreakingSound(BlockState state, @Local(argsOnly = true) BlockPos pos) {
        if (!(state.getBlock() instanceof SlabLike slab) || slab.isUnmixed()) return state;
        return slab.getHalf(state, Objects.requireNonNull(this.minecraft.level), pos, slab.isHitTowards(state, pos, Objects.requireNonNull(this.minecraft.hitResult)));
    }
}
