package dev.micalobia.full_slabs.mixin.entity;

import dev.micalobia.full_slabs.block.Blocks;
import dev.micalobia.full_slabs.block.FullSlabBlock;
import dev.micalobia.full_slabs.block.VerticalSlabBlock;
import dev.micalobia.full_slabs.block.entity.FullSlabBlockEntity;
import dev.micalobia.full_slabs.block.enums.SlabState;
import dev.micalobia.full_slabs.util.LinkedSlabs;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
	public LivingEntityMixin(EntityType<?> type, World world) {
		super(type, world);
	}

	@Redirect(method = "fall", at = @At(value = "NEW", target = "net/minecraft/particle/BlockStateParticleEffect"))
	public BlockStateParticleEffect createFullSlabBlockStateParticleEffect(ParticleType<BlockStateParticleEffect> type, BlockState blockState) {
		if(blockState.isOf(Blocks.FULL_SLAB_BLOCK)) {
			FullSlabBlockEntity entity = (FullSlabBlockEntity) this.world.getBlockEntity(new BlockPos(
					MathHelper.floor(this.getX()),
					MathHelper.ceil(this.getY()) - 1,
					MathHelper.floor(this.getZ())
			));
			if(entity == null) return new BlockStateParticleEffect(type, blockState);
			Axis axis = blockState.get(FullSlabBlock.AXIS);
			switch(axis) {
				case Y:
					return new BlockStateParticleEffect(type, entity.getPositiveSlab().getDefaultState().with(SlabBlock.TYPE, SlabType.TOP));
				case X: {
					boolean positive = this.getX() - entity.getPos().getX() > 0.5d;
					return new BlockStateParticleEffect(type,
							LinkedSlabs
									.vertical(positive ? entity.getPositiveSlab() : entity.getNegativeSlab())
									.getDefaultState()
									.with(VerticalSlabBlock.STATE, positive ? SlabState.POSITIVE : SlabState.NEGATIVE)
									.with(VerticalSlabBlock.AXIS, Axis.X)
					);
				}
				default: // Case Z
					boolean positive = this.getZ() - entity.getPos().getZ() > 0.5d;
					return new BlockStateParticleEffect(type,
							LinkedSlabs
									.vertical(positive ? entity.getPositiveSlab() : entity.getNegativeSlab())
									.getDefaultState()
									.with(VerticalSlabBlock.STATE, positive ? SlabState.POSITIVE : SlabState.NEGATIVE)
									.with(VerticalSlabBlock.AXIS, Axis.Z)
					);
			}
		}
		return new BlockStateParticleEffect(type, blockState);
	}
}
