package dev.micalobia.full_slabs.mixin.entity;

import dev.micalobia.full_slabs.FullSlabsMod;
import dev.micalobia.full_slabs.block.entity.FullSlabBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@Shadow
	public World world;

	@Shadow
	public abstract Vec3d getPos();

	@ModifyArg(method = "spawnSprintingParticles", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V"), index = 0)
	private ParticleEffect fullSlabSprintParticles(ParticleEffect particleEffect) {
		BlockStateParticleEffect effect = (BlockStateParticleEffect) particleEffect;
		BlockState state = effect.getBlockState();
		ParticleType<BlockStateParticleEffect> type = effect.getType();
		if(!state.isOf(FullSlabsMod.FULL_SLAB_BLOCK))
			return new BlockStateParticleEffect(type, state);
		Vec3d hit = getPos().subtract(0d, 0.2d, 0d);
		BlockPos pos = new BlockPos(hit);
		FullSlabBlockEntity entity = (FullSlabBlockEntity) world.getBlockEntity(pos);
		assert entity != null;
		BlockState particleState = entity.getSlabState(hit);
		return new BlockStateParticleEffect(type, particleState);
	}
}
