package dev.micalobia.fullslabs.fabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import dev.micalobia.fullslabs.SlabRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ExplosionDamageCalculator.class)
public class ExplosionDamageCalculatorMixin {
    @ModifyReceiver(method = "getBlockExplosionResistance", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getBlock()Lnet/minecraft/world/level/block/Block;"))
    private BlockState mixedSlabBlastResistence(BlockState state, Explosion explosion, BlockGetter level, BlockPos pos) {
        if (!state.is(SlabRegistry.MIXED_SLAB)) return state;
        return SlabRegistry.MIXED_SLAB.getHalves(state, level, pos).merge((a, b) -> {
            var ar = a.getBlock().getExplosionResistance();
            var br = b.getBlock().getExplosionResistance();
            return ar > br ? a : b;
        });
    }
}
