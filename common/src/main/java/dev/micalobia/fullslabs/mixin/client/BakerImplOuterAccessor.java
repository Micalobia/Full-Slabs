package dev.micalobia.fullslabs.mixin.client;

import net.minecraft.client.render.model.ModelBaker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.client.render.model.ModelBaker$BakerImpl")
public interface BakerImplOuterAccessor {
    @Accessor("field_40571")
    ModelBaker fullslabs$getOuter();
}
