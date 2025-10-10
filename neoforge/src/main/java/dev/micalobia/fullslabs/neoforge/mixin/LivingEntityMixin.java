package dev.micalobia.fullslabs.neoforge.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import dev.micalobia.fullslabs.ducks.LivingEntityDuck;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements LivingEntityDuck {
    @Definition(id = "BlockStateParticleEffect", type = BlockStateParticleEffect.class)
    @Expression("new BlockStateParticleEffect(?, ?, ?)")
    @ModifyArg(method = "fall", at = @At("MIXINEXTRAS:EXPRESSION"))
    private BlockState mixedSlabLandingParticles(BlockState state, @Local(argsOnly = true) BlockPos landedPosition) {
        return this.fullslabs$getMixedLandingState(state, landedPosition);
    }
}
