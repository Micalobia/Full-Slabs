package dev.micalobia.fullslabs.mixin;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.block.Oxidizable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Oxidizable.class)
public interface OxidizableMixin {
    @SuppressWarnings("rawtypes")
    @Inject(method = "method_34740", at = @At("RETURN"), cancellable = true)
    private static void createOxidationLevelIncreasesMap(CallbackInfoReturnable<BiMap> cir) {
        cir.setReturnValue(HashBiMap.create(cir.getReturnValue()));
    }
}
