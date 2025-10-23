package dev.micalobia.fullslabs.mixin.client;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import dev.micalobia.fullslabs.SlabRegistry;
import dev.micalobia.fullslabs.handlers.MixedContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Objects;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin implements BlockView {
    @Shadow
    @Final
    private MinecraftClient client;

    @Definition(id = "getBlockState", method = "Lnet/minecraft/client/world/ClientWorld;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;")
    @Expression("? = ?.getBlockState(?)")
    @ModifyVariable(method = "spawnBlockBreakingParticle", at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER))
    private BlockState mixedSlabBreakingParticles(BlockState state, @Local(argsOnly = true) BlockPos pos) {
        var mixed = SlabRegistry.MIXED_SLAB.get();
        if (!state.isOf(mixed)) return state;
        var crosshair = Objects.requireNonNull(this.client.crosshairTarget).getPos();
        return mixed.forwardSideValue(this, pos, crosshair, MixedContext.Sided::state);
    }
}
