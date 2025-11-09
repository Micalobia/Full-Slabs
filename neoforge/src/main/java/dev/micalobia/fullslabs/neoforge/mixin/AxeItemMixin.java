package dev.micalobia.fullslabs.neoforge.mixin;

import dev.micalobia.fullslabs.ducks.AxeItemDuck;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(AxeItem.class)
public abstract class AxeItemMixin implements AxeItemDuck {
    @Shadow
    protected abstract Optional<BlockState> evaluateNewBlockState(Level par1, BlockPos par2, Player par3, BlockState par4, UseOnContext par5);

    @Override
    public Optional<BlockState> fullslabs$strippedState(Level world, BlockPos pos, @Nullable Player player, BlockState state, UseOnContext context) {
        return this.evaluateNewBlockState(world, pos, player, state, context);
    }
}

