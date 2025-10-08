package dev.micalobia.fullslabs.mixin.client;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import dev.micalobia.fullslabs.SlabRegistry;
import dev.micalobia.fullslabs.block.entity.MixedSlabBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ParticleManager.class)
public class ParticleManagerMixin {
    @Shadow
    protected ClientWorld world;

    @Definition(id = "getBlockState", method = "Lnet/minecraft/client/world/ClientWorld;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;")
    @Expression("? = ?.getBlockState(?)")
    @ModifyVariable(method = "addBlockBreakingParticles", at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER))
    private BlockState mixedSlabBreakingParticles(BlockState state, @Local(argsOnly = true) BlockPos pos) {
        var mixed = SlabRegistry.MIXED_SLAB.get();
        if (!state.isOf(mixed)) return state;
        if (!(this.world.getBlockEntity(pos) instanceof MixedSlabBlockEntity entity)) return state;
        var client = MinecraftClient.getInstance();
        if (!(client.crosshairTarget instanceof BlockHitResult crosshair)) return state;
        return entity.getTargetedState(crosshair);
    }
}
