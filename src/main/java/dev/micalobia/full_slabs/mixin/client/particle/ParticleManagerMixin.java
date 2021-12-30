package dev.micalobia.full_slabs.mixin.client.particle;

import dev.micalobia.full_slabs.FullSlabsMod;
import dev.micalobia.full_slabs.block.entity.ExtraSlabBlockEntity;
import dev.micalobia.full_slabs.block.entity.FullSlabBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Objects;

@Environment(EnvType.CLIENT)
@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {
	@ModifyArg(method = "addBlockBreakingParticles", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/BlockDustParticle;<init>(Lnet/minecraft/client/world/ClientWorld;DDDDDDLnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;)V"), index = 7)
	private BlockState changeFullSlabParticleState(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, BlockState state, BlockPos pos) {
		if(!state.isOf(FullSlabsMod.FULL_SLAB_BLOCK) && !state.isOf(FullSlabsMod.EXTRA_SLAB_BLOCK))
			return state;
		MinecraftClient mc = MinecraftClient.getInstance();
		Objects.requireNonNull(mc.crosshairTarget);
		Vec3d hit = mc.crosshairTarget.getPos();
		BlockEntity entity = world.getBlockEntity(pos);
		if(entity instanceof FullSlabBlockEntity fullEntity) return fullEntity.getSlabState(hit);
		else if(entity instanceof ExtraSlabBlockEntity extraEntity) return extraEntity.getState(hit);
		else return state;
	}
}
