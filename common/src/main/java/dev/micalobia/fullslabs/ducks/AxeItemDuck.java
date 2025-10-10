package dev.micalobia.fullslabs.ducks;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface AxeItemDuck {
    Optional<BlockState> fullslabs$strippedState(World world, BlockPos pos, @Nullable PlayerEntity player, BlockState state, ItemUsageContext context);
}
