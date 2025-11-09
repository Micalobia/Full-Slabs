package dev.micalobia.fullslabs.ducks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface AxeItemDuck {
    Optional<BlockState> fullslabs$strippedState(Level world, BlockPos pos, @Nullable Player player, BlockState state, UseOnContext context);
}
