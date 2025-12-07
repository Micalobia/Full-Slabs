package dev.micalobia.fullslabs.util;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class Utility {
    public static HitResult crosshair(Player player) {
        return player.pick(player.blockInteractionRange(), 1f, false);
    }

    public static HitResult crosshair(@Nullable Player player, boolean isClient) {
        if (isClient) return Minecraft.getInstance().hitResult;
        if (player == null) throw new IllegalArgumentException("Player is null on serverside!");
        return crosshair(player);
    }

    public static Optional<Block> getWaxed(Block unwaxed) {
        return Optional.ofNullable(HoneycombItem.WAXABLES.get().get(unwaxed));
    }
}

