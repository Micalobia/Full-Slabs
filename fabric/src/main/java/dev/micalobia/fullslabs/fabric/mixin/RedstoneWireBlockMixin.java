package dev.micalobia.fullslabs.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.micalobia.fullslabs.SlabRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.enums.WireConnection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(RedstoneWireBlock.class)
public class RedstoneWireBlockMixin {
    @Unique
    private static BlockView fullslabs$world;

    @Unique
    private static BlockPos fullslabs$pos;

    @Inject(method = "getRenderConnectionType(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;Z)Lnet/minecraft/block/enums/WireConnection;", at = @At("HEAD"))
    private void skimWorldAndPos(BlockView world, BlockPos pos, Direction direction, boolean bl, CallbackInfoReturnable<WireConnection> cir) {
        fullslabs$world = world;
        fullslabs$pos = pos;
    }

    @WrapOperation(method = "connectsTo(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;emitsRedstonePower()Z"))
    private static boolean mixedSlabRedstonePower(BlockState state, Operation<Boolean> original, BlockState ignored, Direction direction) {
        var mixed = SlabRegistry.MIXED_SLAB.get();
        if (!state.isOf(mixed)) return original.call(state);
        return mixed.emitsRedstonePower(fullslabs$world, fullslabs$pos.offset(direction == null ? Direction.DOWN : direction));
    }
}
