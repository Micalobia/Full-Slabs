package dev.micalobia.full_slabs.mixin.entity;

import dev.micalobia.full_slabs.FullSlabsMod;
import dev.micalobia.full_slabs.block.entity.FullSlabBlockEntity;
import dev.micalobia.full_slabs.util.MixinSelf;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public class LivingEntityMixin implements MixinSelf<LivingEntity> {
	@Redirect(method = "fall", at = @At(value = "NEW", target = "net/minecraft/particle/BlockStateParticleEffect"))
	private BlockStateParticleEffect interceptLandingFullSlabParticle(ParticleType<BlockStateParticleEffect> type, BlockState state, double heightDifference, boolean onGround, BlockState landedState, BlockPos pos) {
		if(!state.isOf(FullSlabsMod.FULL_SLAB_BLOCK))
			return new BlockStateParticleEffect(type, state);
		FullSlabBlockEntity entity = (FullSlabBlockEntity) self().world.getBlockEntity(pos);
		assert entity != null;
		BlockState landState = entity.getSlabState(self().getPos());
		return new BlockStateParticleEffect(type, landState);
	}
}
