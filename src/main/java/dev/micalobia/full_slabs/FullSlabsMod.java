package dev.micalobia.full_slabs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.micalobia.full_slabs.block.FullSlabBlock;
import dev.micalobia.full_slabs.block.entity.FullSlabBlockEntity;
import dev.micalobia.full_slabs.config.TiltConfig;
import dev.micalobia.full_slabs.util.Utility;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.fabric.api.event.registry.RegistryIdRemapCallback;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
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

import java.util.Set;


public class FullSlabsMod implements ModInitializer {
	public static final String MOD_ID = "full_slabs";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	public static BlockEntityType<FullSlabBlockEntity> FULL_SLAB_BLOCK_ENTITY;
	public static Block FULL_SLAB_BLOCK;
	public static Gson GSON = new
			GsonBuilder()
			.registerTypeAdapter(TiltConfig.class, new TiltConfig.Deserializer())
			.create();
	public static Set<Identifier> TILTED_SLABS;

	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		FULL_SLAB_BLOCK = Registry.register(Registry.BLOCK, id("full_slab_block"), new FullSlabBlock(Settings.copy(Blocks.BEDROCK)));
		FULL_SLAB_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, id("full_slab"), FabricBlockEntityTypeBuilder.create(FullSlabBlockEntity::new, FULL_SLAB_BLOCK).build());

		RegistryIdRemapCallback.event(Registry.BLOCK).register((state -> StateRefresher.INSTANCE.reorderBlockStates()));
		Utility.injectBlockProperty(SlabBlock.class, Properties.AXIS, Axis.Y);
		RegistryEntryAddedCallback.event(Registry.BLOCK).register(((rawId, id, object) -> {
			if (object instanceof SlabBlock) Utility.injectBlockProperty(SlabBlock.class, Properties.AXIS, Axis.Y);
		}));
		StateRefresher.INSTANCE.reorderBlockStates();
	}
}
