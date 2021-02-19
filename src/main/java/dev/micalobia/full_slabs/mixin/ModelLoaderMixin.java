package dev.micalobia.full_slabs.mixin;

import dev.micalobia.full_slabs.client.render.model.VerticalSlabModel;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ModelLoader.class)
public class ModelLoaderMixin {
//	@Inject(method = "loadModelFromJson", at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/ResourceManager;getResource(Lnet/minecraft/util/Identifier;)Lnet/minecraft/resource/Resource;"), cancellable = true)
//	public void loadModelFromJson(Identifier id, CallbackInfoReturnable<JsonUnbakedModel> cir) {
//		System.out.println(id);
//		if (!"full_slabs".equals(id.getNamespace())) return;
//		System.out.print(id + " ======================");
//		String modelJson = VerticalSlabModel.createBlockModelJson(id.getPath(), "block");
//		JsonUnbakedModel model = JsonUnbakedModel.deserialize(modelJson);
//		model.id = id.toString();
//		cir.setReturnValue(model);
//	}
}
