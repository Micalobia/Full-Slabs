package dev.micalobia.fullslabs.fabric.compat.slabsandstairs;

import dev.micalobia.fullslabs.block.VerticalSlabBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

// https://github.com/FrostbyteGames1/Frostbytes-Slabs-and-Stairs/blob/485b3ed/src/main/java/net/frostbyte/slabsandstairs/block/custom/ice/IceSlabBlock.java
public class IceVerticalSlabBlock extends VerticalSlabBlock {
    public IceVerticalSlabBlock(SlabBlock block, Properties properties) {
        super(block, properties);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
        if (!EnchantmentHelper.hasTag(tool, EnchantmentTags.PREVENTS_ICE_MELTING)) {
            if (level.dimension().equals(Level.NETHER)) {
                level.removeBlock(pos, false);
                return;
            }
            var blockState = level.getBlockState(pos.below());
            if (blockState.blocksMotion() || blockState.liquid()) level.setBlockAndUpdate(pos, IceBlock.meltsInto());
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBrightness(LightLayer.BLOCK, pos) > 11 - state.getLightBlock()) this.melt(level, pos);
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacentState, Direction direction) {
        return adjacentState.is(this) || super.skipRendering(state, adjacentState, direction);
    }

    protected void melt(Level world, BlockPos pos) {
        if (world.dimension().equals(Level.NETHER)) world.removeBlock(pos, false);
        else {
            world.setBlockAndUpdate(pos, IceBlock.meltsInto());
            world.neighborChanged(pos, IceBlock.meltsInto().getBlock(), null);
        }
    }
}
