package dev.micalobia.fullslabs.fabric.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import dev.micalobia.fullslabs.ducks.LivingEntityDuck;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements LivingEntityDuck {
    @Definition(id = "BlockParticleOption", type = BlockParticleOption.class)
    @Expression("new BlockParticleOption(?, ?)")
    @ModifyArg(method = "checkFallDamage", at = @At("MIXINEXTRAS:EXPRESSION"))
    private BlockState mixedSlabLandingParticles(BlockState state, @Local(argsOnly = true) BlockPos landedPosition) {
        return this.fullslabs$getMixedLandingState(state, landedPosition);
    }
}
