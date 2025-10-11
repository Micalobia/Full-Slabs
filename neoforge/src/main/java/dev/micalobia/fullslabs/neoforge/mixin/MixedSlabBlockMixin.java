package dev.micalobia.fullslabs.neoforge.mixin;

import dev.micalobia.fullslabs.block.MixedSlabBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.annotation.MethodsReturnNonnullByDefault;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;
import net.minecraft.world.explosion.Explosion;
import net.neoforged.neoforge.common.extensions.IBlockExtension;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.ParametersAreNonnullByDefault;

// This class is to override anything in IBlockExtension, since I can't do that in common
@Mixin(MixedSlabBlock.class)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MixedSlabBlockMixin implements IBlockExtension {
    @Shadow
    public abstract <T> T act(BlockView world, BlockPos pos, MixedSlabBlock.MixedFunction<T> function);

    @Override
    public float getExplosionResistance(BlockState state, BlockView world, BlockPos pos, Explosion explosion) {
        return this.act(world, pos, mixed -> {
            var towards = mixed.getTowardsState();
            var away = mixed.getAwayState();
            var towardsResistance = towards.getExplosionResistance(world, pos, explosion);
            var awayResistance = away.getExplosionResistance(world, pos, explosion);
            return Math.max(towardsResistance, awayResistance);
        });
    }

    @Override
    public BlockSoundGroup getSoundType(BlockState state, WorldView world, BlockPos pos, @Nullable Entity entity) {
        if (!(entity instanceof PlayerEntity && MinecraftClient.getInstance().crosshairTarget instanceof BlockHitResult crosshair))
            return IBlockExtension.super.getSoundType(state, world, pos, entity);
        return this.act(world, pos, mixed -> mixed.getTargetedState(crosshair).getSoundType(world, pos, entity));
    }
}
