package dev.micalobia.fullslabs.neoforge.mixin;

import dev.micalobia.fullslabs.block.MixedSlabBlock;
import dev.micalobia.fullslabs.ducks.MixedSlabBlockDuck;
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

import javax.annotation.ParametersAreNonnullByDefault;

// This class is to override anything in IBlockExtension, since I can't do that in common
@Mixin(MixedSlabBlock.class)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MixedSlabBlockMixin implements IBlockExtension, MixedSlabBlockDuck {
    @Override
    public float getExplosionResistance(BlockState state, BlockView world, BlockPos pos, Explosion explosion) {
        return this.forwardSidesValue(world, pos, ctx -> ctx.state().getExplosionResistance(world, pos, explosion), Math::max);
    }

    @Override
    public BlockSoundGroup getSoundType(BlockState state, WorldView world, BlockPos pos, @Nullable Entity entity) {
        if (!(entity instanceof PlayerEntity && MinecraftClient.getInstance().crosshairTarget instanceof BlockHitResult crosshair))
            return IBlockExtension.super.getSoundType(state, world, pos, entity);
        return this.forwardSideValue(world, pos, crosshair.getPos(), ctx -> ctx.state().getSoundType(world, pos, entity));
    }
}
