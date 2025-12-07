package dev.micalobia.fullslabs.neoforge.mixin;

import dev.micalobia.fullslabs.block.MixedSlabBlock;
import dev.micalobia.fullslabs.block.SlabLike;
import dev.micalobia.fullslabs.util.Utility;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.extensions.IBlockExtension;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.ParametersAreNonnullByDefault;

// This class is to override anything in IBlockExtension, since I can't do that in common
@Mixin(MixedSlabBlock.class)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MixedSlabBlockMixin implements IBlockExtension, SlabLike {
    @Shadow
    public abstract boolean isSignalSource(BlockState state, BlockGetter level, BlockPos pos);

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        return getHalves(state, level, pos).map(s -> s.getExplosionResistance(level, pos, explosion)).merge(Math::max);
    }

    @Override
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        if (!(entity instanceof Player player))
            return IBlockExtension.super.getSoundType(state, level, pos, entity);
        var crosshair = Utility.crosshair(player, level.isClientSide());
        return getHalf(state, level, pos, crosshair).getSoundType(level, pos, entity);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData, Player player) {
        var crosshair = Utility.crosshair(player, level.isClientSide());
        var half = getHalf(state, level, pos, crosshair);
        return half.getCloneItemStack(pos, level, includeData, player);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
        return this.isSignalSource(state, level, pos);
    }
}
