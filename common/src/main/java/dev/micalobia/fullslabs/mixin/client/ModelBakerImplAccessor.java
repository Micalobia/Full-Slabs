package dev.micalobia.fullslabs.mixin.client;

import net.minecraft.client.resources.model.ModelBakery;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.client.resources.model.ModelBakery$ModelBakerImpl")
public interface ModelBakerImplAccessor {
    @Accessor("field_40571")
    ModelBakery fullslabs$getOuter();
}
