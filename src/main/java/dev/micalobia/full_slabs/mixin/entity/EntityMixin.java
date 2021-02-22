package dev.micalobia.full_slabs.mixin.entity;

import dev.micalobia.full_slabs.block.Blocks;
import dev.micalobia.full_slabs.block.FullSlabBlock;
import dev.micalobia.full_slabs.block.entity.FullSlabBlockEntity;
import dev.micalobia.full_slabs.util.Helper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Random;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@Shadow
	public World world;
	@Shadow
	@Final
	protected Random random;
	@Shadow
	private EntityDimensions dimensions;

	@Shadow
	public abstract double getX();

	@Shadow
	public abstract double getY();

	@Shadow
	public abstract double getZ();

	@Shadow
	public abstract Vec3d getVelocity();

	@Shadow
	public abstract Vec3d getPos();

	@Inject(method = "spawnSprintingParticles", cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT, at = @At("JUMP"))
	public void spawnSlabSprintingParticles(CallbackInfo ci, int i, int j, int k, BlockPos blockPos, BlockState blockState) {
		if(blockState.isOf(Blocks.FULL_SLAB_BLOCK)) {
			Vec3d position = this.getPos().subtract(0, 0.20000000298023224d, 0);
			FullSlabBlockEntity entity = (FullSlabBlockEntity) this.world.getBlockEntity(blockPos);
			Axis axis = blockState.get(FullSlabBlock.AXIS);
			boolean isPositive = Helper.isPositive(position, blockPos, axis);
			Block slab = isPositive ? entity.getPositiveSlab() : entity.getNegativeSlab();
			BlockState state = Helper.getState(slab, axis, isPositive);
			Vec3d velocity = this.getVelocity();
			this.world.addParticle(new BlockStateParticleEffect(ParticleTypes.BLOCK, state), this.getX() + (this.random.nextDouble() - 0.5D) * (double) this.dimensions.width, this.getY() + 0.1D, this.getZ() + (this.random.nextDouble() - 0.5D) * (double) this.dimensions.width, velocity.x * -4.0D, 1.5D, velocity.z * -4.0D);
			ci.cancel();
		}
	}
}
