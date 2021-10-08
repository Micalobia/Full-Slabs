package dev.micalobia.full_slabs;

import dev.micalobia.full_slabs.block.ExtraSlabBlock;
import dev.micalobia.full_slabs.block.FullSlabBlock;
import dev.micalobia.full_slabs.block.entity.ExtraSlabBlockEntity;
import dev.micalobia.full_slabs.block.entity.ExtraSlabBlockEntity.SlabExtra;
import dev.micalobia.full_slabs.block.entity.FullSlabBlockEntity;
import dev.micalobia.full_slabs.config.ModConfig;
import dev.micalobia.full_slabs.util.Utility;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
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
		FULL_SLAB_BLOCK = Registry.register(Registry.BLOCK, id("full_slab_block"), new FullSlabBlock(Settings.copy(Blocks.BEDROCK).nonOpaque().luminance(FullSlabBlock::stateToLuminance)));
		FULL_SLAB_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, id("full_slab"), FabricBlockEntityTypeBuilder.create(FullSlabBlockEntity::new, FULL_SLAB_BLOCK).build());

		EXTRA_SLAB_BLOCK = Registry.register(Registry.BLOCK, id("extra_slab_block"), new ExtraSlabBlock(Settings.copy(Blocks.BEDROCK).luminance(ExtraSlabBlock::stateToLuminance)));
		EXTRA_SLAB_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, id("extra_slab"), FabricBlockEntityTypeBuilder.create(ExtraSlabBlockEntity::new, EXTRA_SLAB_BLOCK).build());
		AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
		TILTED_SLABS = AutoConfig.getConfigHolder(ModConfig.class).getConfig().getTiltableSlabs();

		Utility.injectBlockProperty(SlabBlock.class, Properties.AXIS, Axis.Y);
		RegistryEntryAddedCallback.event(Registry.BLOCK).register(((rawId, id, block) -> {
			if(block instanceof SlabBlock) {
				Utility.injectBlockProperty(block, Properties.AXIS, Axis.Y);
				StateRefresher.INSTANCE.reorderBlockStates();
			}
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
