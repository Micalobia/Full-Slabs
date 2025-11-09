package dev.micalobia.fullslabs.mixin;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.item.HoneycombItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HoneycombItem.class)
public class HoneycombItemMixin {
    @SuppressWarnings("rawtypes")
    @ModifyReturnValue(method = "method_34723", at = @At("RETURN"))
    private static BiMap createUnwaxedToWaxedMap(BiMap original) {
        return HashBiMap.create(original);
    }
}
