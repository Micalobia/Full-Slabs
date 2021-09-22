package dev.micalobia.full_slabs.mixin.entity;

import dev.micalobia.full_slabs.FullSlabsMod;
import dev.micalobia.full_slabs.block.entity.FullSlabBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@Shadow
	public World world;

	@Shadow
	public abstract double getX();

	@Shadow
	public abstract double getY();

	@Shadow
	public abstract double getZ();

	@Shadow
	public abstract Vec3d getPos();

	@Redirect(method = "spawnSprintingParticles", at = @At(value = "NEW", target = "net/minecraft/particle/BlockStateParticleEffect"))
	private BlockStateParticleEffect fullSlabSprintParticles(ParticleType<BlockStateParticleEffect> type, BlockState state) {
		if(!state.isOf(FullSlabsMod.FULL_SLAB_BLOCK))
			return new BlockStateParticleEffect(type, state);
		int x = MathHelper.floor(getX());
		int y = MathHelper.floor(getY() - 0.2d);
		int z = MathHelper.floor(getZ());
		BlockPos pos = new BlockPos(x, y, z);
		FullSlabBlockEntity entity = (FullSlabBlockEntity) world.getBlockEntity(pos);
		Vec3d hit = getPos().subtract(0d, 0.2d, 0d);
		assert entity != null;
		BlockState particleState = entity.getSlabState(hit);
		return new BlockStateParticleEffect(type, particleState);
	}
}
