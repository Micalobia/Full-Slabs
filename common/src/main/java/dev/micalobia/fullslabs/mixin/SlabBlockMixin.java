package dev.micalobia.fullslabs.mixin;

import dev.micalobia.fullslabs.SlabRegistry;
import dev.micalobia.fullslabs.block.MixedSlabBlock;
import dev.micalobia.fullslabs.block.SlabLike;
import dev.micalobia.fullslabs.block.VerticalSlabBlock;
import dev.micalobia.fullslabs.config.Controls;
import dev.micalobia.fullslabs.handlers.MixedHandlers;
import dev.micalobia.fullslabs.util.MixedType;
import dev.micalobia.fullslabs.util.SlabPlacement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@SuppressWarnings("AddedMixinMembersNamePattern") // To mute the SlabLike warnings
@Mixin(SlabBlock.class)
public class SlabBlockMixin implements SlabLike {
    @Shadow
    @Final
    public static BooleanProperty WATERLOGGED;

    @Inject(method = "getStateForPlacement", at = @At("HEAD"), cancellable = true)
    private void editPlacementRules(BlockPlaceContext ctx, CallbackInfoReturnable<BlockState> cir) {
        if (!hasVertical()) return;
        var self = fullslabs$self();
        var pos = ctx.getClickedPos();
        var level = ctx.getLevel();
        var state = level.getBlockState(pos);
        var block = state.getBlock();
        if (block instanceof SlabLike slab && slab.isUnmixed()) {
            if (slab.getRoot() == self) cir.setReturnValue(slab.asDouble(state));
            else
                cir.setReturnValue(SlabRegistry.MIXED_SLAB.defaultBlockState().setValue(MixedSlabBlock.TYPE, slab.getType(state)));
            return;
        }
        var face = ctx.getClickedFace();
        var fluidState = level.getFluidState(pos);
        var player = ctx.getPlayer();
        final Direction target;
        if (player == null) target = Direction.DOWN;
        else {
            var mode = Controls.getPlacementMode(ctx.getPlayer().getUUID());
            target = SlabPlacement.getTargetedDirection(mode, face, ctx.getHorizontalDirection(), pos, ctx.getClickLocation());
        }
        cir.setReturnValue(SlabPlacement.getTargetedState(fullslabs$self(), face, target, ctx.getRotation()).setValue(BlockStateProperties.WATERLOGGED, fluidState.is(Fluids.WATER)));
    }

    @Inject(method = "canBeReplaced", at = @At("HEAD"), cancellable = true)
    private void editReplacementRules(BlockState state, BlockPlaceContext context, CallbackInfoReturnable<Boolean> cir) {
        var self = fullslabs$self();
        if (!VerticalSlabBlock.hasVertical(self)) return;
        // This keeps the code a bit tidier imo
        validate:
        {
            var stack = context.getItemInHand();
            var type = state.getValue(BlockStateProperties.SLAB_TYPE);
            if (type == SlabType.DOUBLE) break validate;
            if (!(stack.getItem() instanceof BlockItem blockItem)) break validate;
            var block = blockItem.getBlock();
            if (!(block instanceof SlabBlock)) break validate;
            if (block != self && !(MixedHandlers.hasHandler(block) && MixedHandlers.hasHandler(self))) break validate;
            if (context.replacingClickedOnBlock()) {
                cir.setReturnValue(isInside(state, context.getClickedPos(), context.getClickLocation()));
                return;
            }
            cir.setReturnValue(true);
            return;
        }
        cir.setReturnValue(false);
    }

    @Unique
    private SlabBlock fullslabs$self() {
        return (SlabBlock) (Object) this;
    }

    // SlabLike impl

    @Override
    public BlockState getHalf(BlockState state, BlockGetter level, BlockPos pos, boolean isTowards) {
        var empty = state.getValue(BlockStateProperties.WATERLOGGED) ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
        return switch (state.getValue(BlockStateProperties.SLAB_TYPE)) {
            case TOP -> isTowards ? state : empty;
            case BOTTOM -> isTowards ? empty : state;
            case DOUBLE -> state.setValue(BlockStateProperties.SLAB_TYPE, isTowards ? SlabType.TOP : SlabType.BOTTOM);
        };
    }

    @Override
    public boolean isVanilla() {
        return true;
    }

    @Override
    public boolean isVertical() {
        return false;
    }

    @Override
    public boolean isMixed() {
        return false;
    }

    @Override
    public boolean supportsMixing() {
        return MixedHandlers.hasHandler(fullslabs$self());
    }

    @Override
    public boolean hasVertical() {
        return VerticalSlabBlock.hasVertical(fullslabs$self());
    }

    @Override
    public boolean isDouble(BlockState state) {return state.getValue(BlockStateProperties.SLAB_TYPE) == SlabType.DOUBLE;}

    @Override
    public boolean isSingle(BlockState state) {return state.getValue(BlockStateProperties.SLAB_TYPE) != SlabType.DOUBLE;}

    @Override
    public boolean isTowards(BlockState state) {return state.getValue(BlockStateProperties.SLAB_TYPE) == SlabType.TOP;}

    @Override
    public MixedType getType(BlockState state) {return MixedType.VERTICAL;}

    @Override
    public Direction getDirection(BlockState state) {
        return switch (state.getValue(BlockStateProperties.SLAB_TYPE)) {
            case TOP -> Direction.UP;
            case BOTTOM -> Direction.DOWN;
            case DOUBLE -> throw new IllegalArgumentException("Not a half-slab!");
        };
    }

    @Override
    public SlabBlock getRoot() {return fullslabs$self();}

    @Override
    public BlockState asDouble(BlockState state) {return state.setValue(BlockStateProperties.SLAB_TYPE, SlabType.DOUBLE).setValue(WATERLOGGED, false);}

    @Override
    public boolean isInside(BlockState state, BlockPos pos, Vec3 hit) {
        var type = state.getValue(BlockStateProperties.SLAB_TYPE);
        if (type == SlabType.DOUBLE) return false;
        var diff = hit.y - pos.getY();
        return type == SlabType.BOTTOM ? diff >= 0.5d : diff <= 0.5d;
    }
}
