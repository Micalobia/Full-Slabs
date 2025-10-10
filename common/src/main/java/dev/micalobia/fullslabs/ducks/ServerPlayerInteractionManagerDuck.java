package dev.micalobia.fullslabs.ducks;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Nullable;

public interface ServerPlayerInteractionManagerDuck {
    void fullslabs$onPlayerDestroyItem(PlayerEntity player, ItemStack stack, @Nullable Hand hand);
}
