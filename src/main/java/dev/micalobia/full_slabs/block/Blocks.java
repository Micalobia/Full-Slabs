package dev.micalobia.full_slabs.block;

import dev.micalobia.full_slabs.block.entity.FullSlabBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.registry.Registry;

public class Blocks {
	public static final FullSlabBlock FULL_SLAB_BLOCK;
	public static final BlockEntityType<FullSlabBlockEntity> FULL_SLAB_BLOCK_ENTITY;

	static {
		FULL_SLAB_BLOCK = register("full_slabs:full_slab_block", new FullSlabBlock(FabricBlockSettings.copyOf(net.minecraft.block.Blocks.BEDROCK)));
		FULL_SLAB_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "full_slabs:full_slab", BlockEntityType.Builder.create(FullSlabBlockEntity::new, Blocks.FULL_SLAB_BLOCK).build(null));
	}

	private static <T extends Block> T register(String id, T block) {
		return Registry.register(Registry.BLOCK, id, block);
	}
}
