package dev.micalobia.fullslabs.fabric.compat.blockus;

import com.brand.blockus.blocks.base.asphalt.AsphaltBlock;
import dev.micalobia.fullslabs.block.VerticalSlabBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;

public class AsphaltVerticalSlabBlock extends VerticalSlabBlock {
    public AsphaltVerticalSlabBlock(SlabBlock block, Properties settings) {
        super(block, settings);
    }

    @Override
    public void stepOn(Level world, BlockPos pos, BlockState state, Entity entity) {
        AsphaltBlock.applySprintEffect(entity);
    }
}
