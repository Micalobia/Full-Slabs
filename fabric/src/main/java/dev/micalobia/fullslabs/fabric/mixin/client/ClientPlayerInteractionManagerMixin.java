package dev.micalobia.fullslabs.fabric.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.sugar.Local;
import dev.micalobia.fullslabs.SlabRegistry;
import dev.micalobia.fullslabs.block.entity.MixedSlabBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Objects;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @ModifyReceiver(method = "updateBlockBreakingProgress", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getSoundGroup()Lnet/minecraft/sound/BlockSoundGroup;"))
    private BlockState mixedSlabBreakingSound(BlockState state, @Local(argsOnly = true) BlockPos pos) {
        var mixed = SlabRegistry.MIXED_SLAB.get();
        if (!state.isOf(mixed)) return state;
        var world = Objects.requireNonNull(this.client.world);
        var entity = world.getBlockEntity(pos);
        if (!(entity instanceof MixedSlabBlockEntity mixedEntity)) return state;
        var crosshair = Objects.requireNonNull(this.client.crosshairTarget);
        return mixedEntity.getState(mixed.towards(state, crosshair.getPos(), pos));
    }
}
