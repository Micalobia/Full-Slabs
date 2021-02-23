package dev.micalobia.full_slabs.mixin.client;

import dev.micalobia.full_slabs.client.render.model.FullSlabModel;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
	@Inject(method = "reloadResources", at = @At("HEAD"))
	public void print(CallbackInfoReturnable<CompletableFuture<Void>> cir) {
		FullSlabModel.clearCachedMeshes();
	}
}
