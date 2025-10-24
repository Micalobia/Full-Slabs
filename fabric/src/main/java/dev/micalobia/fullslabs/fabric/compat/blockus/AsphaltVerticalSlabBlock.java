package dev.micalobia.fullslabs.fabric.compat.blockus;

import com.brand.blockus.blocks.base.asphalt.AsphaltBlock;
import dev.micalobia.fullslabs.block.VerticalSlabBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AsphaltVerticalSlabBlock extends VerticalSlabBlock {
    public AsphaltVerticalSlabBlock(SlabBlock block, Settings settings) {
        super(block, settings);
    }

    @Override
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
        AsphaltBlock.applySprintEffect(entity);
    }
}
