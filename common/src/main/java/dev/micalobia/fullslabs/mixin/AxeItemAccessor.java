package dev.micalobia.fullslabs.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;

@Mixin(AxeItem.class)
public interface AxeItemAccessor {
    @Invoker("tryStrip")
    Optional<BlockState> fullslabs$tryStrip(World world, BlockPos pos, @Nullable PlayerEntity player, BlockState state);
}
