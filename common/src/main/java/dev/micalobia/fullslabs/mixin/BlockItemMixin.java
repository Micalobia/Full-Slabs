package dev.micalobia.fullslabs.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.micalobia.fullslabs.SlabRegistry;
import dev.micalobia.fullslabs.block.MixedSlabBlock.MixedType;
import dev.micalobia.fullslabs.block.VerticalSlabBlock;
import dev.micalobia.fullslabs.block.entity.MixedSlabBlockEntity;
import dev.micalobia.fullslabs.ducks.BlockItemDuck;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.SlabType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.property.Properties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin implements BlockItemDuck {
    @Unique
    private BlockState fullslabs$placed;

    @Override
    public BlockState fullslabs$getPlaced() {
        return fullslabs$placed;
    }

    @Inject(method = "place(Lnet/minecraft/item/ItemPlacementContext;Lnet/minecraft/block/BlockState;)Z", at = @At("HEAD"))
    private void skimMixedSlabs(ItemPlacementContext context, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (!state.isOf(SlabRegistry.MIXED_SLAB.get())) return;
        var world = context.getWorld();
        var pos = context.getBlockPos();
        var stack = context.getStack();
        var currentState = world.getBlockState(pos);
        var type = MixedType.fromState(currentState);
        var currentBlock = VerticalSlabBlock.getRoot(currentState.getBlock());
        var towardsCurrent = switch (type) {
            case NORTH, SOUTH, EAST, WEST ->
                    currentState.get(VerticalSlabBlock.TYPE) == VerticalSlabBlock.VerticalType.TOWARDS;
            case VERTICAL -> currentState.get(Properties.SLAB_TYPE) == SlabType.TOP;
        };
        var placedBlock = VerticalSlabBlock.getRoot(((BlockItem) stack.getItem()).getBlock());
        this.fullslabs$placed = type.state(placedBlock, !towardsCurrent);
        MixedSlabBlockEntity.writeCache(
                towardsCurrent ? currentBlock : placedBlock,
                towardsCurrent ? placedBlock : currentBlock
        );
    }
}
