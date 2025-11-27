package dev.micalobia.fullslabs.fabric.mixin.client;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import dev.micalobia.fullslabs.SlabRegistry;
import dev.micalobia.fullslabs.util.SlabContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Objects;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin implements BlockGetter {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Definition(id = "getBlockState", method = "Lnet/minecraft/client/multiplayer/ClientLevel;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;")
    @Expression("? = ?.getBlockState(?)")
    @ModifyVariable(method = "addBreakingBlockEffect", at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER))
    private BlockState mixedSlabBreakingParticles(BlockState state, @Local(argsOnly = true) BlockPos pos) {
        if (!state.is(SlabRegistry.MIXED_SLAB)) return state;
        var crosshair = Objects.requireNonNull(this.minecraft.hitResult).getLocation();
        return SlabRegistry.MIXED_SLAB.forwardSideValue(this, pos, crosshair, SlabContext::mainState);
    }
}
