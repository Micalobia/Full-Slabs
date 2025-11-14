package dev.micalobia.fullslabs.fabric.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.sugar.Local;
import dev.micalobia.fullslabs.SlabRegistry;
import dev.micalobia.fullslabs.block.entity.MixedSlabBlockEntity;
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
        if (!state.is(SlabRegistry.MIXED_SLAB)) return state;
        var world = Objects.requireNonNull(this.minecraft.level);
        var entity = world.getBlockEntity(pos);
        if (!(entity instanceof MixedSlabBlockEntity mixedEntity)) return state;
        var crosshair = Objects.requireNonNull(this.minecraft.hitResult);
        return mixedEntity.getState(SlabRegistry.MIXED_SLAB.towards(state, crosshair.getLocation(), pos));
    }
}
