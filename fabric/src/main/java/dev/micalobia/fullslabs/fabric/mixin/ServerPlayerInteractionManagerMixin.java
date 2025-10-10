package dev.micalobia.fullslabs.fabric.mixin;

import dev.micalobia.fullslabs.ducks.ServerPlayerInteractionManagerDuck;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin implements ServerPlayerInteractionManagerDuck {
    @Override
    public void fullslabs$onPlayerDestroyItem(PlayerEntity player, ItemStack stack, @Nullable Hand hand) {
        // no-op
    }
}
