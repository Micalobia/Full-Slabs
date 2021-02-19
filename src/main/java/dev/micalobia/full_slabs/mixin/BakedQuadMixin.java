package dev.micalobia.full_slabs.mixin;

import dev.micalobia.full_slabs.client.render.model.IBakedQuad;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BakedQuad.class)
public class BakedQuadMixin implements IBakedQuad {
	@Shadow @Final protected Sprite sprite;

	public Sprite getSprite() {
		return sprite;
	}
}
