package dev.micalobia.fullslabs.mixin;

import dev.micalobia.fullslabs.SlabRegistry;
import dev.micalobia.fullslabs.block.SlabLike;
import dev.micalobia.fullslabs.block.VerticalSlabBlock;
import dev.micalobia.fullslabs.block.entity.MixedSlabBlockEntity;
import dev.micalobia.fullslabs.ducks.BlockItemDuck;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin implements BlockItemDuck {
    @Unique
    private BlockState fullslabs$placed;

    @Override
    public BlockState fullslabs$getPlaced() {
        return this.fullslabs$placed;
    }

    @Inject(method = "placeBlock", at = @At("HEAD"))
    private void skimMixedSlabs(BlockPlaceContext context, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (!state.is(SlabRegistry.MIXED_SLAB)) return;
        var world = context.getLevel();
        var pos = context.getClickedPos();
        var stack = context.getItemInHand();
        var currentState = world.getBlockState(pos);
        var slab = (SlabLike) currentState.getBlock();
        var type = slab.getType(currentState);
        var currentRoot = slab.getRoot();
        var currentTowards = slab.isTowards(currentState);
        var placedBlock = VerticalSlabBlock.getRoot(((BlockItem) stack.getItem()).getBlock());
        this.fullslabs$placed = type.state(placedBlock, !currentTowards);
        MixedSlabBlockEntity.writeCache(
                currentTowards ? currentRoot : placedBlock,
                currentTowards ? placedBlock : currentRoot
        );
    }
}
