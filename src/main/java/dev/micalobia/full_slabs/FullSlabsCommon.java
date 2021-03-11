package dev.micalobia.full_slabs;

import dev.micalobia.full_slabs.block.Blocks;
import dev.micalobia.full_slabs.event.Events;
import net.fabricmc.api.ModInitializer;

public class FullSlabsCommon implements ModInitializer {
	@Override
	public void onInitialize() {
		//TODO: Call the static initilizer in a better way
		Blocks.init();
		Events.init();
	}
}
