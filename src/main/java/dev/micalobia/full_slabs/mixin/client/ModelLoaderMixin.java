package dev.micalobia.full_slabs.mixin.client;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import dev.micalobia.full_slabs.FullSlabsMod;
import dev.micalobia.full_slabs.mixin.client.render.model.json.JsonUnbakedModelAccessor;
import dev.micalobia.full_slabs.util.MixinSelf;
import dev.micalobia.full_slabs.util.Utility;
import net.fabricmc.fabric.impl.client.model.ModelLoaderHooks;
import net.minecraft.block.Block;
import net.minecraft.block.SlabBlock;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelElement;
import net.minecraft.client.render.model.json.ModelElementFace;
import net.minecraft.client.render.model.json.ModelVariantMap;
import net.minecraft.client.render.model.json.ModelVariantMap.DeserializationContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Mixin(ModelLoader.class)
public abstract class ModelLoaderMixin implements MixinSelf<ModelLoader> {
	private static final String templateBlockstateJson = "{\"variants\":{\"type=double\":{\"model\":\"%s\"},\"type=bottom,axis=y\":{\"model\":\"%s\"},\"type=top,axis=y\":{\"model\":\"%s\"},\"type=bottom,axis=z\":{\"model\":\"%s\"},\"type=bottom,axis=x\":{\"model\":\"%s\"},\"type=top,axis=z\":{\"model\":\"%s\"},\"type=top,axis=x\":{\"model\":\"%s\"}}}";
	private static final String templateTiltedBlockstateJson = "{\"variants\":{\"type=double,axis=y\":{\"model\":\"%1$s\"},\"type=bottom,axis=y\":{\"model\":\"%2$s\"},\"type=top,axis=y\":{\"model\":\"%3$s\"},\"type=double,axis=x\":{\"model\":\"%1$s\",\"x\":90,\"y\":90},\"type=bottom,axis=x\":{\"model\":\"%2$s\",\"x\":90,\"y\":90},\"type=top,axis=x\":{\"model\":\"%3$s\",\"x\":90,\"y\":90},\"type=double,axis=z\":{\"model\":\"%1$s\",\"x\":270},\"type=bottom,axis=z\":{\"model\":\"%2$s\",\"x\":270},\"type=top,axis=z\":{\"model\":\"%3$s\",\"x\":270}}}";
	private static final String templateModelJson = "{\"parent\":\"%s\",\"textures\":{\"bottom\":\"%s\",\"top\":\"%s\",\"side\":\"%s\"}}";
	boolean processingSlab = false;
	Identifier createUsing;
	Map<Identifier, Direction> needToCreate;
	Map<Direction, String> creationFaces;
	@Shadow
	@Final
	private DeserializationContext variantMapDeserializationContext;
	@Shadow
	@Final
	private Map<Identifier, UnbakedModel> unbakedModels;

	private static Identifier getVariantLocation(ModelVariantMap map, String variant) {
		return map.getVariant(variant).getVariants().get(0).getLocation();
	}

	private static Identifier append(Identifier prefix, String suffix) {
		return FullSlabsMod.id(prefix.getPath() + suffix);
	}

	private static JsonUnbakedModel fetchParent(JsonUnbakedModel model, Function<Identifier, UnbakedModel> loader) {
		JsonUnbakedModelAccessor accessor = ((JsonUnbakedModelAccessor) model);
		JsonUnbakedModel parent = accessor.getParent();
		if(parent == null) parent = (JsonUnbakedModel) loader.apply(accessor.getParentId());
		return parent;
	}

	private static Map<Direction, String> getDirectionalFaces(JsonUnbakedModel model, Function<Identifier, UnbakedModel> loader) {
		List<ModelElement> elements;
		JsonUnbakedModel prev = model;
		JsonUnbakedModel checking = model;
		Map<Direction, String> mapping = new HashMap<>();
		while(true) {
			elements = checking.getElements();
			if(!elements.isEmpty()) {
				ModelElement element = elements.get(0);
				Map<Direction, ModelElementFace> faces = element.faces;
				for(Direction key : faces.keySet()) {
					ModelElementFace face = faces.get(key);
					mapping.put(key, face.textureId);
				}
				break;
			} else {
				prev = checking;
				checking = fetchParent(checking, loader);
			}
		}
		for(Direction key : mapping.keySet()) {
			String str = mapping.get(key);
			if(str.startsWith("#")) {
				mapping.put(key, prev.resolveSprite(str.substring(1)).getTextureId().toString());
			}
		}
		return mapping;
	}

	@Shadow
	protected abstract JsonUnbakedModel loadModelFromJson(Identifier id) throws IOException;

	private String createBlockstateJson(Identifier bottomId, Identifier topId, Identifier doubleId) {
		Identifier northId = append(bottomId, "_north");
		Identifier eastId = append(bottomId, "_east");
		Identifier southId = append(bottomId, "_south");
		Identifier westId = append(bottomId, "_west");
		needToCreate = ImmutableMap.of(
				northId, Direction.NORTH,
				eastId, Direction.EAST,
				southId, Direction.SOUTH,
				westId, Direction.WEST
		);
		createUsing = bottomId;
		return String.format(templateBlockstateJson,
				doubleId,
				bottomId,
				topId,
				northId,
				eastId,
				southId,
				westId
		);
	}

	private String createTiltedBlockstateJson(Identifier bottomId, Identifier topId, Identifier doubleId) {
		return String.format(templateTiltedBlockstateJson, doubleId, bottomId, topId);
	}

	private String createModelJson(Identifier parent, String bottom, String top, String side) {
		return String.format(templateModelJson, parent, bottom, top, side);
	}

	private JsonUnbakedModel fetchJsonModel(Identifier id) throws IOException {
		if(this.unbakedModels.containsKey(id)) return (JsonUnbakedModel) this.unbakedModels.get(id);
		return this.loadModelFromJson(id);
	}

	@Redirect(method = "loadModel", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;", ordinal = 0))
	private Iterator<Pair<String, ModelVariantMap>> replaceSlabVariantFiles(List<Pair<String, ModelVariantMap>> list, Identifier id) {
		try {
			Identifier pure = new Identifier(id.getNamespace(), id.getPath());
			Block block = Registry.BLOCK.get(pure);
			processingSlab = false;
			creationFaces = null;
			needToCreate = null;
			//  && slabKeys.equals(list.get(0).getSecond().getVariantMap().keySet())
			if(block instanceof SlabBlock) {
				processingSlab = true;
				needToCreate = new HashMap<>();
				boolean tilted = Utility.tilted(pure);
				JsonUnbakedModel creationModel = fetchJsonModel(getVariantLocation(list.get(0).getSecond(), "type=bottom"));
				creationFaces = getDirectionalFaces(creationModel, ((ModelLoaderHooks) self())::fabric_loadModel);
				for(int i = 0; i < list.size(); ++i) {
					Pair<String, ModelVariantMap> pair = list.get(i);
					ModelVariantMap map = pair.getSecond();
					String jsonStr = tilted ?
							createTiltedBlockstateJson(
									getVariantLocation(map, "type=bottom"),
									getVariantLocation(map, "type=top"),
									getVariantLocation(map, "type=double")
							) :
							createBlockstateJson(
									getVariantLocation(map, "type=bottom"),
									getVariantLocation(map, "type=top"),
									getVariantLocation(map, "type=double")
							);
					StringReader reader = new StringReader(jsonStr);
					list.set(i, Pair.of(pair.getFirst(), ModelVariantMap.fromJson(variantMapDeserializationContext, reader)));
					reader.close();
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return list.iterator();
	}

	@Redirect(method = "loadModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/model/ModelLoader;loadModelFromJson(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/model/json/JsonUnbakedModel;", ordinal = 0))
	private JsonUnbakedModel interceptSlabLoad(ModelLoader loader, Identifier id) throws IOException {
		if(!processingSlab || !needToCreate.containsKey(id)) return this.loadModelFromJson(id);
		Direction direction = needToCreate.get(id);
		String bottom = creationFaces.get(Direction.DOWN);
		String top = creationFaces.get(Direction.UP);
		String side = creationFaces.get(Direction.NORTH);
		String jsonStr = switch(direction) {
			case DOWN, UP -> "{}";
			case NORTH -> createModelJson(FullSlabsMod.id("block/slab_north"), bottom, top, side);
			case EAST -> createModelJson(FullSlabsMod.id("block/slab_east"), bottom, top, side);
			case SOUTH -> createModelJson(FullSlabsMod.id("block/slab_south"), bottom, top, side);
			case WEST -> createModelJson(FullSlabsMod.id("block/slab_west"), bottom, top, side);
		};
		return JsonUnbakedModel.deserialize(jsonStr);
	}
}