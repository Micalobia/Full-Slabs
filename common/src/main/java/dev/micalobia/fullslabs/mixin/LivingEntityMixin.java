package dev.micalobia.fullslabs.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import dev.micalobia.fullslabs.SlabRegistry;
import dev.micalobia.fullslabs.block.entity.MixedSlabBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Definition(id = "BlockStateParticleEffect", type = BlockStateParticleEffect.class)
    @Expression("new BlockStateParticleEffect(?, ?)")
    @ModifyArg(method = "fall", at = @At("MIXINEXTRAS:EXPRESSION"))
    private BlockState mixedSlabLandingParticles(BlockState state, @Local(argsOnly = true) BlockPos landedPosition) {
        var mixed = SlabRegistry.MIXED_SLAB.get();
        if (!(state.isOf(mixed))) return state;
        var self = (LivingEntity) (Object) this;
        var blockEntity = self.getWorld().getBlockEntity(landedPosition);
        if (!(blockEntity instanceof MixedSlabBlockEntity mixedEntity)) return state;
        return mixedEntity.getState(mixed.towards(state, self.getPos(), landedPosition));
    }
}
