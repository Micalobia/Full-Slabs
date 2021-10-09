package dev.micalobia.full_slabs.mixin.item;

import net.minecraft.block.Block;
import net.minecraft.item.WallStandingBlockItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WallStandingBlockItem.class)
public interface WallStandingBlockItemAccessor {
	@Accessor
	Block getWallBlock();
}
