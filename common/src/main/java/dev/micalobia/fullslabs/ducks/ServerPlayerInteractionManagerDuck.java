package dev.micalobia.fullslabs.ducks;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface ServerPlayerInteractionManagerDuck {
    void fullslabs$onPlayerDestroyItem(Player player, ItemStack stack, @Nullable InteractionHand hand);
}
