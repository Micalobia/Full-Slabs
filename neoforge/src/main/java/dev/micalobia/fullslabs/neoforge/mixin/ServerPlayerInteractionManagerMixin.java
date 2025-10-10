package dev.micalobia.fullslabs.neoforge.mixin;

import dev.micalobia.fullslabs.ducks.ServerPlayerInteractionManagerDuck;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.Hand;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin implements ServerPlayerInteractionManagerDuck {
    @Override
    public void fullslabs$onPlayerDestroyItem(PlayerEntity player, ItemStack stack, @Nullable Hand hand) {
        EventHooks.onPlayerDestroyItem(player, stack, hand);
    }
}
