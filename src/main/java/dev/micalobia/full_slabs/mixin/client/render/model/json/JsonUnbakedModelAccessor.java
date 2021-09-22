package dev.micalobia.full_slabs.mixin.client.render.model.json;

import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(JsonUnbakedModel.class)
public interface JsonUnbakedModelAccessor {
	@Accessor
	JsonUnbakedModel getParent();

	@Accessor
	Identifier getParentId();
}
