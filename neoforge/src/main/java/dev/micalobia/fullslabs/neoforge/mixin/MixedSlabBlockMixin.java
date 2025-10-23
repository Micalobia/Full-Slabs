package dev.micalobia.fullslabs.neoforge.mixin;

import dev.micalobia.fullslabs.block.MixedSlabBlock;
import dev.micalobia.fullslabs.ducks.MixedSlabBlockDuck;
import dev.micalobia.fullslabs.util.Utility;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.annotation.MethodsReturnNonnullByDefault;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
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
public abstract class MixedSlabBlockMixin implements IBlockExtension, MixedSlabBlockDuck {
    @Shadow
    public abstract boolean emitsRedstonePower(BlockView world, BlockPos pos);

    @Override
    public float getExplosionResistance(BlockState state, BlockView world, BlockPos pos, Explosion explosion) {
        return this.forwardSidesValue(world, pos, ctx -> ctx.state().getExplosionResistance(world, pos, explosion), Math::max);
    }

    @Override
    public BlockSoundGroup getSoundType(BlockState state, WorldView world, BlockPos pos, @Nullable Entity entity) {
        if (!(entity instanceof PlayerEntity player))
            return IBlockExtension.super.getSoundType(state, world, pos, entity);
        var crosshair = Utility.crosshair(player, world.isClient());
        return this.forwardSideValue(world, pos, crosshair.getPos(), ctx -> ctx.state().getSoundType(world, pos, entity));
    }

    @Override
    public ItemStack getCloneItemStack(WorldView world, BlockPos pos, BlockState state, boolean includeData, PlayerEntity player) {
        var crosshair = Utility.crosshair(player, world.isClient());
        return forwardSideValue(world, pos, crosshair.getPos(), ctx -> new ItemStack(ctx.block().asItem()));
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockView world, BlockPos pos, @Nullable Direction direction) {
        return this.emitsRedstonePower(world, pos);
    }
}
