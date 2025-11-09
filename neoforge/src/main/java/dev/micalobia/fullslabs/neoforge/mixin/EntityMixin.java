package dev.micalobia.fullslabs.neoforge.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import dev.micalobia.fullslabs.ducks.EntityDuck;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityDuck {
    @SuppressWarnings("LocalMayBeArgsOnly") // The method doesn't even have args
    @Definition(id = "BlockParticleOption", type = BlockParticleOption.class)
    @Expression("new BlockParticleOption(?, ?, ?)")
    @ModifyArg(method = "spawnSprintParticle", at = @At("MIXINEXTRAS:EXPRESSION"))
    private BlockState mixedSlabSprintingParticles(BlockState state, @Local(ordinal = 0) BlockPos pos) {
        return this.fullslabs$tryGetMixedState(state, pos);
    }
}
