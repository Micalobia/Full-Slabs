package dev.micalobia.full_slabs.config;

import com.google.common.collect.ImmutableSet;
import com.google.gson.*;
import dev.micalobia.full_slabs.FullSlabsMod;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TiltConfig {
	protected boolean overritePrev;
	protected Set<String> entries;

	protected TiltConfig(boolean replace, Collection<String> values) {
		overritePrev = replace;
		entries = Set.copyOf(values);
	}

	public static TiltConfig fromResource(Resource resource) throws IOException {
		try(InputStreamReader reader = new InputStreamReader(resource.getInputStream())) {
			return FullSlabsMod.GSON.fromJson(reader, TiltConfig.class);
		}
	}

	public static ImmutableSet<Identifier> coalesce(Collection<TiltConfig> configs) {
		HashSet<Identifier> ret = new HashSet<>();
		for(TiltConfig config : configs) {
			if(config.overritePrev) ret = new HashSet<>();
			ret.addAll(config.entries.stream().map(Identifier::new).collect(Collectors.toUnmodifiableSet()));
		}
		return ImmutableSet.copyOf(ret);
	}

	public static class Deserializer implements JsonDeserializer<TiltConfig> {
		@Override
		public TiltConfig deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			try {
				if(json.isJsonObject()) {
					JsonObject object = json.getAsJsonObject();
					boolean overrite = object.has("replace") && object.get("replace").getAsBoolean();
					String[] entries = FullSlabsMod.GSON.fromJson(object.get("values"), String[].class);
					return new TiltConfig(overrite, List.of(entries));
				} else {
					FullSlabsMod.LOGGER.error(json);
					throw new Exception("Not an object!");
				}
			} catch(Exception e) {
				throw new JsonParseException("Not a valid tilted_slab config file; Check the wiki for info! " + e.getMessage());
			}
		}
	}
}
