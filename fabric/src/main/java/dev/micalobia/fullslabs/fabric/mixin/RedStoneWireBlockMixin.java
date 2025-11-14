package dev.micalobia.fullslabs.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.micalobia.fullslabs.SlabRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(RedStoneWireBlock.class)
public class RedStoneWireBlockMixin {
    @Unique
    private static BlockGetter fullslabs$world;

    @Unique
    private static BlockPos fullslabs$pos;

    @Inject(method = "getConnectingSide(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Z)Lnet/minecraft/world/level/block/state/properties/RedstoneSide;", at = @At("HEAD"))
    private void skimWorldAndPos(BlockGetter world, BlockPos pos, Direction direction, boolean bl, CallbackInfoReturnable<RedstoneSide> cir) {
        fullslabs$world = world;
        fullslabs$pos = pos;
    }

    @WrapOperation(method = "shouldConnectTo(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isSignalSource()Z"))
    private static boolean mixedSlabRedstonePower(BlockState state, Operation<Boolean> original, BlockState ignored, Direction direction) {
        if (!state.is(SlabRegistry.MIXED_SLAB)) return original.call(state);
        return SlabRegistry.MIXED_SLAB.isSignalSource(fullslabs$world, fullslabs$pos.relative(direction == null ? Direction.DOWN : direction));
    }
}
