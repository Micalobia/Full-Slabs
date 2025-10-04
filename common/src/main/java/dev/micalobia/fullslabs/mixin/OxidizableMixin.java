package dev.micalobia.fullslabs.mixin;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.block.Oxidizable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Oxidizable.class)
public interface OxidizableMixin {
    @SuppressWarnings("rawtypes")
    @ModifyReturnValue(method = "method_34740", at = @At("RETURN"))
    private static BiMap createOxidationLevelIncreasesMap(BiMap original) {
        return HashBiMap.create(original);
    }
}
