package dev.micalobia.full_slabs.mixin.client.particle;

import dev.micalobia.full_slabs.block.Blocks;
import dev.micalobia.full_slabs.block.FullSlabBlock;
import dev.micalobia.full_slabs.block.entity.FullSlabBlockEntity;
import dev.micalobia.full_slabs.util.Helper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.BlockDustParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {
	@Shadow protected ClientWorld world;

	@Shadow @Final private Random random;

	@Shadow public abstract void addParticle(Particle particle);

	@Inject(method = "addBlockBreakingParticles", cancellable = true, at = @At("HEAD"))
	public void addSlabBreakingParticles(BlockPos pos, Direction direction, CallbackInfo ci) {
		BlockState fullState = this.world.getBlockState(pos);
		if (!fullState.isOf(Blocks.FULL_SLAB_BLOCK)) return;
		FullSlabBlockEntity entity = (FullSlabBlockEntity) world.getBlockEntity(pos);
		Vec3d hit = MinecraftClient.getInstance().crosshairTarget.getPos();
		Axis axis = fullState.get(FullSlabBlock.AXIS);
		boolean positive = Helper.isPositive(hit, pos, axis);
		Block slab = positive ? entity.getPositiveSlab() : entity.getNegativeSlab();
		BlockState state = Helper.getState(slab, axis, positive);

		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		float f = 0.1f;
		Box box = state.getOutlineShape(this.world, pos).getBoundingBox();
		double dx = x + this.random.nextDouble() * (box.getXLength() - .2d) + .1d + box.minX;
		double dy = y + this.random.nextDouble() * (box.getYLength() - .2d) + .1d + box.minY;
		double dz = z + this.random.nextDouble() * (box.getZLength() - .2d) + .1d + box.minZ;
		switch(direction) {
			case DOWN: dy = y + box.minY - .1d; break;
			case UP: dy = y + box.maxY + .1d; break;
			case NORTH: dz = z + box.minZ - .1d; break;
			case SOUTH: dz = z + box.maxZ + .1d; break;
			case WEST: dx = x + box.minX - .1d; break;
			case EAST: dx = x + box.maxX	+ .1d; break;
		}
		this.addParticle((new BlockDustParticle(this.world, dx, dy, dz, 0.0D, 0.0D, 0.0D, state)).setBlockPos(pos).move(0.2F).scale(0.6F));
		ci.cancel();
	}
}
