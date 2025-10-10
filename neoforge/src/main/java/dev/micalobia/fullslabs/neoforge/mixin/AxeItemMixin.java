package dev.micalobia.fullslabs.neoforge.mixin;

import dev.micalobia.fullslabs.ducks.AxeItemDuck;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(AxeItem.class)
public abstract class AxeItemMixin implements AxeItemDuck {
    @Shadow
    protected abstract Optional<BlockState> evaluateNewBlockState(World par1, BlockPos par2, PlayerEntity par3, BlockState par4, ItemUsageContext par5);

    @Override
    public Optional<BlockState> fullslabs$strippedState(World world, BlockPos pos, @Nullable PlayerEntity player, BlockState state, ItemUsageContext context) {
        return this.evaluateNewBlockState(world, pos, player, state, context);
    }
}

