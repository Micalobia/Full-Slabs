package dev.micalobia.fullslabs.fabric.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.micalobia.fullslabs.SlabRegistry;
import dev.micalobia.fullslabs.ducks.BlockItemDuck;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(BlockItem.class)
public class BlockItemMixin {
    @ModifyArg(method = "place", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BlockItem;getPlaceSound(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/sounds/SoundEvent;"))
    private BlockState mixedSlabPlacementSounds(BlockState state, @Local(argsOnly = true) BlockPlaceContext context) {
        if (!(state.is(SlabRegistry.MIXED_SLAB))) return state;
        if (this instanceof BlockItemDuck self)
            return self.fullslabs$getPlaced() == null ? state : self.fullslabs$getPlaced();
        throw new AssertionError();
    }
}
