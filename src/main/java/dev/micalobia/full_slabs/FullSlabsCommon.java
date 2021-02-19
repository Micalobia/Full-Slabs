package dev.micalobia.full_slabs;

import dev.micalobia.full_slabs.block.Blocks;
import net.fabricmc.api.ModInitializer;

public class FullSlabsCommon implements ModInitializer {
	@Override
	public void onInitialize() {
		new Blocks(); //TODO: Call the static initilizer in a better way
	}
}
