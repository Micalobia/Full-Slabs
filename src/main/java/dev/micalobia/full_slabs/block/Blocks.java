package dev.micalobia.full_slabs.block;

import dev.micalobia.full_slabs.block.entity.FullSlabBlockEntity;
import dev.micalobia.full_slabs.util.Helper;
import dev.micalobia.full_slabs.util.LinkedSlabs;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockView;

public class Blocks {
	public static final FullSlabBlock FULL_SLAB_BLOCK;
	public static final BlockEntityType<FullSlabBlockEntity> FULL_SLAB_BLOCK_ENTITY;

	private static final Identifier AIR = new Identifier("minecraft:air");

	static {
		Registry.BLOCK.forEach(Blocks::generateVerticalPair);
		RegistryEntryAddedCallback.event(Registry.BLOCK).register(Blocks::hookIntoRegister);
		FULL_SLAB_BLOCK = register("full_slabs:full_slab_block", new FullSlabBlock(FabricBlockSettings.copyOf(net.minecraft.block.Blocks.BEDROCK).nonOpaque().solidBlock(Blocks::never)));
		FULL_SLAB_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "full_slabs:full_slab", BlockEntityType.Builder.create(FullSlabBlockEntity::new, Blocks.FULL_SLAB_BLOCK).build(null));
	}

	public static void init() {
	}

	private static boolean never(BlockState state, BlockView world, BlockPos pos) {
		return false;
	}

	private static <T extends Block> T register(String id, T block) {
		return Registry.register(Registry.BLOCK, id, block);
	}

	private static void hookIntoRegister(int i, Identifier identifier, Block block) {
		generateVerticalPair(identifier, block);
	}

	private static void generateVerticalPair(Identifier base, Block block) {
		if(!(block instanceof SlabBlock)) return;
		Identifier vertical = new Identifier("full_slabs", base.getNamespace() + "_" + base.getPath() + "_vertical");
		Block verticalSlab = register(vertical.toString(), new VerticalSlabBlock(block));
		LinkedSlabs.link(block, verticalSlab);
	}

	private static void generateVerticalPair(Block block) {
		generateVerticalPair(Helper.fetchId(block), block);
	}
}
