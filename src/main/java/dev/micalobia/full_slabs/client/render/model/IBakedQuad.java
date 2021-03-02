package dev.micalobia.full_slabs.client.render.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.Sprite;

@Environment(EnvType.CLIENT)
public interface IBakedQuad {
	Sprite getSprite();
}
