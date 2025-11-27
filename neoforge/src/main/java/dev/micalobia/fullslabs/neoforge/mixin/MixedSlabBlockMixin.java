package dev.micalobia.fullslabs.neoforge.mixin;

import dev.micalobia.fullslabs.block.MixedSlabBlock;
import dev.micalobia.fullslabs.ducks.MixedSlabBlockDuck;
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
public abstract class MixedSlabBlockMixin implements IBlockExtension, MixedSlabBlockDuck {
    @Shadow
    public abstract boolean isSignalSource(BlockGetter world, BlockPos pos);

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter world, BlockPos pos, Explosion explosion) {
        return this.forwardSidesValue(world, pos, ctx -> ctx.mainState().getExplosionResistance(world, pos, explosion), Math::max);
    }

    @Override
    public SoundType getSoundType(BlockState state, LevelReader world, BlockPos pos, @Nullable Entity entity) {
        if (!(entity instanceof Player player))
            return IBlockExtension.super.getSoundType(state, world, pos, entity);
        var crosshair = Utility.crosshair(player, world.isClientSide());
        return this.forwardSideValue(world, pos, crosshair.getLocation(), ctx -> ctx.mainState().getSoundType(world, pos, entity));
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state, boolean includeData, Player player) {
        var crosshair = Utility.crosshair(player, world.isClientSide());
        return forwardSideValue(world, pos, crosshair.getLocation(), ctx -> new ItemStack(ctx.mainBlock().asItem()));
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, @Nullable Direction direction) {
        return this.isSignalSource(world, pos);
    }
}
