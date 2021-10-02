package dev.micalobia.full_slabs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.micalobia.full_slabs.block.ExtraSlabBlock;
import dev.micalobia.full_slabs.block.FullSlabBlock;
import dev.micalobia.full_slabs.block.entity.ExtraSlabBlockEntity;
import dev.micalobia.full_slabs.block.entity.ExtraSlabBlockEntity.SlabExtra;
import dev.micalobia.full_slabs.block.entity.FullSlabBlockEntity;
import dev.micalobia.full_slabs.config.TiltConfig;
import dev.micalobia.full_slabs.util.Utility;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.fabric.api.event.registry.RegistryIdRemapCallback;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import virtuoel.statement.api.StateRefresher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Set;


public class FullSlabsMod implements ModInitializer {
	public static final String MOD_ID = "full_slabs";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	public static final Gson GSON = new
			GsonBuilder()
			.registerTypeAdapter(TiltConfig.class, new TiltConfig.Deserializer())
			.registerTypeAdapter(SlabExtra.class, new SlabExtra.Deserializer())
			.create();
	private static final String defaultExtraConfig = "[\r\n  {\r\n    \"block\": \"minecraft:wall_torch\",\r\n    \"north\": \"facing=south\",\r\n    \"south\": \"facing=north\",\r\n    \"east\": \"facing=west\",\r\n    \"west\": \"facing=east\"\r\n  },\r\n  {\r\n    \"block\": \"minecraft:torch\",\r\n    \"bottom\": \"\"\r\n  },\r\n  {\r\n    \"block\": \"minecraft:soul_wall_torch\",\r\n    \"north\": \"facing=south\",\r\n    \"south\": \"facing=north\",\r\n    \"east\": \"facing=west\",\r\n    \"west\": \"facing=east\"\r\n  },\r\n  {\r\n    \"block\": \"minecraft:soul_torch\",\r\n    \"bottom\": \"\"\r\n  },\r\n  {\r\n    \"block\": \"minecraft:lantern\",\r\n    \"top\": \"hanging=true\",\r\n    \"bottom\": \"hanging=false\"\r\n  },\r\n  {\r\n    \"block\": \"minecraft:soul_lantern\",\r\n    \"top\": \"hanging=true\",\r\n    \"bottom\": \"hanging=false\"\r\n  },\r\n  {\r\n    \"block\": \"snow\",\r\n    \"bottom\": \"layers=1\"\r\n  },\r\n  {\r\n    \"block\": \"white_carpet\",\r\n    \"bottom\": \"\"\r\n  },\r\n  {\r\n    \"block\": \"orange_carpet\",\r\n    \"bottom\": \"\"\r\n  },\r\n  {\r\n    \"block\": \"magenta_carpet\",\r\n    \"bottom\": \"\"\r\n  },\r\n  {\r\n    \"block\": \"light_blue_carpet\",\r\n    \"bottom\": \"\"\r\n  },\r\n  {\r\n    \"block\": \"yellow_carpet\",\r\n    \"bottom\": \"\"\r\n  },\r\n  {\r\n    \"block\": \"lime_carpet\",\r\n    \"bottom\": \"\"\r\n  },\r\n  {\r\n    \"block\": \"pink_carpet\",\r\n    \"bottom\": \"\"\r\n  },\r\n  {\r\n    \"block\": \"gray_carpet\",\r\n    \"bottom\": \"\"\r\n  },\r\n  {\r\n    \"block\": \"light_gray_carpet\",\r\n    \"bottom\": \"\"\r\n  },\r\n  {\r\n    \"block\": \"cyan_carpet\",\r\n    \"bottom\": \"\"\r\n  },\r\n  {\r\n    \"block\": \"purple_carpet\",\r\n    \"bottom\": \"\"\r\n  },\r\n  {\r\n    \"block\": \"blue_carpet\",\r\n    \"bottom\": \"\"\r\n  },\r\n  {\r\n    \"block\": \"brown_carpet\",\r\n    \"bottom\": \"\"\r\n  },\r\n  {\r\n    \"block\": \"green_carpet\",\r\n    \"bottom\": \"\"\r\n  },\r\n  {\r\n    \"block\": \"red_carpet\",\r\n    \"bottom\": \"\"\r\n  },\r\n  {\r\n    \"block\": \"black_carpet\",\r\n    \"bottom\": \"\"\r\n  },\r\n  {\r\n    \"block\": \"moss_carpet\",\r\n    \"bottom\": \"\"\r\n  }\r\n]";
	public static BlockEntityType<FullSlabBlockEntity> FULL_SLAB_BLOCK_ENTITY;
	public static BlockEntityType<ExtraSlabBlockEntity> EXTRA_SLAB_BLOCK_ENTITY;
	public static Block FULL_SLAB_BLOCK;
	public static Block EXTRA_SLAB_BLOCK;
	public static Set<Identifier> TILTED_SLABS;

	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		FULL_SLAB_BLOCK = Registry.register(Registry.BLOCK, id("full_slab_block"), new FullSlabBlock(Settings.copy(Blocks.BEDROCK)));
		FULL_SLAB_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, id("full_slab"), FabricBlockEntityTypeBuilder.create(FullSlabBlockEntity::new, FULL_SLAB_BLOCK).build());

		EXTRA_SLAB_BLOCK = Registry.register(Registry.BLOCK, id("extra_slab_block"), new ExtraSlabBlock(Settings.copy(Blocks.BEDROCK).luminance(ExtraSlabBlock::stateToLuminance)));
		EXTRA_SLAB_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, id("extra_slab"), FabricBlockEntityTypeBuilder.create(ExtraSlabBlockEntity::new, EXTRA_SLAB_BLOCK).build());

		SlabExtra[] extras = getOrCreateExtraConfig();
		for(SlabExtra extra : extras) {
			ExtraSlabBlockEntity.allowedExtras.put(Utility.getBlockId(extra.getBlock()), extra);
		}

		RegistryIdRemapCallback.event(Registry.BLOCK).register((state -> StateRefresher.INSTANCE.reorderBlockStates()));
		Utility.injectBlockProperty(SlabBlock.class, Properties.AXIS, Axis.Y);
		RegistryEntryAddedCallback.event(Registry.BLOCK).register(((rawId, id, object) -> {
			if(object instanceof SlabBlock) Utility.injectBlockProperty(SlabBlock.class, Properties.AXIS, Axis.Y);
		}));
		StateRefresher.INSTANCE.reorderBlockStates();
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	private SlabExtra[] getOrCreateExtraConfig() {
		Path configPath = FabricLoader.getInstance().getConfigDir();
		File configFile = configPath.resolve("full_slabs/slab_extras.json").toFile();
		SlabExtra[] ret;
		LOGGER.info(configFile);
		LOGGER.info(configFile.exists());
		if(!configFile.exists()) {
			try {
				configFile.getParentFile().mkdirs();
				configFile.createNewFile();
				try(FileOutputStream out = new FileOutputStream(configFile)) {
					out.write(defaultExtraConfig.getBytes(StandardCharsets.UTF_8));
				}
			} catch(IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
		try(FileReader reader = new FileReader(configFile)) {
			ret = GSON.fromJson(reader, SlabExtra[].class);
		} catch(IOException e) {
			// TODO: Real error handling
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return ret;
	}
}
