package dev.micalobia.fullslabs.neoforge.mixin;

import dev.micalobia.fullslabs.ducks.ServerPlayerInteractionManagerDuck;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin implements ServerPlayerInteractionManagerDuck {
    @Override
    public void fullslabs$onPlayerDestroyItem(Player player, ItemStack stack, @Nullable InteractionHand hand) {
        EventHooks.onPlayerDestroyItem(player, stack, hand);
    }
}
