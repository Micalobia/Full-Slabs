package dev.micalobia.full_slabs.mixin.entity;

import dev.micalobia.full_slabs.FullSlabsMod;
import dev.micalobia.full_slabs.block.entity.FullSlabBlockEntity;
import dev.micalobia.full_slabs.util.MixinSelf;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin implements MixinSelf<LivingEntity> {
	private BlockPos _landedPosition;

	@Inject(method = "fall", at = @At("HEAD"))
	private void skimLandedPosition(double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition, CallbackInfo ci) {
		_landedPosition = landedPosition;
	}

	@ModifyArg(method = "fall", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;spawnParticles(Lnet/minecraft/particle/ParticleEffect;DDDIDDDD)I"), index = 0)
	private ParticleEffect changeFullSlabLandingParticles(ParticleEffect particleEffect) {
		BlockStateParticleEffect effect = (BlockStateParticleEffect) particleEffect;
		BlockState state = effect.getBlockState();
		ParticleType<BlockStateParticleEffect> type = effect.getType();
		if(!state.isOf(FullSlabsMod.FULL_SLAB_BLOCK))
			return new BlockStateParticleEffect(type, state);
		FullSlabBlockEntity entity = (FullSlabBlockEntity) self().world.getBlockEntity(_landedPosition);
		assert entity != null;
		BlockState landState = entity.getSlabState(self().getPos());
		return new BlockStateParticleEffect(type, landState);
	}
}
