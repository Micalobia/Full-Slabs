package dev.micalobia.full_slabs.mixin.client.render.model;

import com.google.common.collect.ImmutableSet;
import dev.micalobia.full_slabs.FullSlabsMod;
import dev.micalobia.full_slabs.config.TiltConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(BakedModelManager.class)
public class BakedModelManagerMixin {
	@Inject(method = "prepare", at = @At(value = "HEAD"))
	private void skim(ResourceManager manager, Profiler profiler, CallbackInfoReturnable<ModelLoader> cir) {
		try {
			List<TiltConfig> tilted = new ArrayList<>();
			List<Resource> resources = manager.getAllResources(FullSlabsMod.id("config/tilted_slabs.json"));
			for(Resource resource : resources) {
				tilted.add(TiltConfig.fromResource(resource));
				resource.close();
			}
			FullSlabsMod.TILTED_SLABS = TiltConfig.coalesce(tilted);
		} catch(Exception e) {
			e.printStackTrace();
			FullSlabsMod.LOGGER.error("Something went wrong loading the tilted slab config; Loading default");
			FullSlabsMod.TILTED_SLABS = ImmutableSet.of(new Identifier("minecraft:smooth_stone_slab"));
		}
		FullSlabsMod.LOGGER.info(FullSlabsMod.TILTED_SLABS);
	}
}
