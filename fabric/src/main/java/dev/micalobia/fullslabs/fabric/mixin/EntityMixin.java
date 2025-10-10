package dev.micalobia.fullslabs.fabric.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import dev.micalobia.fullslabs.ducks.EntityDuck;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityDuck {
    @SuppressWarnings("LocalMayBeArgsOnly") // The method doesn't even have args
    @Definition(id = "BlockStateParticleEffect", type = BlockStateParticleEffect.class)
    @Expression("new BlockStateParticleEffect(?, ?)")
    @ModifyArg(method = "spawnSprintingParticles", at = @At("MIXINEXTRAS:EXPRESSION"))
    private BlockState mixedSlabSprintingParticles(BlockState state, @Local(ordinal = 0) BlockPos pos) {
        return this.fullslabs$tryGetMixedState(state, pos);
    }
}
