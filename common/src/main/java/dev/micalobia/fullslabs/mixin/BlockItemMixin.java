package dev.micalobia.fullslabs.mixin;

import dev.micalobia.fullslabs.SlabRegistry;
import dev.micalobia.fullslabs.block.MixedSlabBlock.MixedType;
import dev.micalobia.fullslabs.block.VerticalSlabBlock;
import dev.micalobia.fullslabs.block.entity.MixedSlabBlockEntity;
import dev.micalobia.fullslabs.ducks.BlockItemDuck;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;
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
        var type = MixedType.fromState(currentState);
        var currentBlock = VerticalSlabBlock.getRoot(currentState.getBlock());
        var towardsCurrent = switch (type) {
            case NORTH, SOUTH, EAST, WEST ->
                    currentState.getValue(VerticalSlabBlock.TYPE) == VerticalSlabBlock.VerticalType.TOWARDS;
            case VERTICAL -> currentState.getValue(BlockStateProperties.SLAB_TYPE) == SlabType.TOP;
        };
        var placedBlock = VerticalSlabBlock.getRoot(((BlockItem) stack.getItem()).getBlock());
        this.fullslabs$placed = type.state(placedBlock, !towardsCurrent);
        MixedSlabBlockEntity.writeCache(
                towardsCurrent ? currentBlock : placedBlock,
                towardsCurrent ? placedBlock : currentBlock
        );
    }
}
