package dev.micalobia.full_slabs.mixin.client.particle;

import dev.micalobia.full_slabs.FullSlabsMod;
import dev.micalobia.full_slabs.block.entity.FullSlabBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.BlockDustParticle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Environment(EnvType.CLIENT)
@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {
	@Redirect(method = "addBlockBreakingParticles", at = @At(value = "NEW", target = "net/minecraft/client/particle/BlockDustParticle"))
	private BlockDustParticle changeFullSlabParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, BlockState state, BlockPos pos) {
		if(!state.isOf(FullSlabsMod.FULL_SLAB_BLOCK))
			return new BlockDustParticle(world, x, y, z, velocityX, velocityY, velocityZ, state, pos);
		MinecraftClient mc = MinecraftClient.getInstance();
		assert mc.crosshairTarget != null;
		Vec3d hit = mc.crosshairTarget.getPos();
		FullSlabBlockEntity entity = (FullSlabBlockEntity) world.getBlockEntity(pos);
		assert entity != null;
		BlockState particleState = entity.getSlabState(hit);
		return new BlockDustParticle(world, x, y, z, velocityX, velocityY, velocityZ, particleState, pos);
	}
}
