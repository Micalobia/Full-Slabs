package dev.micalobia.fullslabs.neoforge.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.micalobia.fullslabs.SlabRegistry;
import dev.micalobia.fullslabs.ducks.BlockItemDuck;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(BlockItem.class)
public class BlockItemMixin {
    @ModifyArg(method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/BlockItem;getPlaceSound(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/sound/SoundEvent;"))
    private BlockState mixedSlabPlacementSounds(BlockState state, @Local(argsOnly = true) ItemPlacementContext context) {
        var mixed = SlabRegistry.MIXED_SLAB.get();
        if (!(state.isOf(mixed))) return state;
        if (this instanceof BlockItemDuck self)
            return self.fullslabs$getPlaced() == null ? state : self.fullslabs$getPlaced();
        throw new AssertionError();
    }
}
