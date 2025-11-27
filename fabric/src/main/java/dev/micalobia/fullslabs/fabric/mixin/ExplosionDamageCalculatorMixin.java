package dev.micalobia.fullslabs.fabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import dev.micalobia.fullslabs.SlabRegistry;
import dev.micalobia.fullslabs.util.SlabContext;
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
    private BlockState mixedSlabBlastResistence(BlockState state, Explosion explosion, BlockGetter world, BlockPos pos) {
        var mixedBlock = SlabRegistry.MIXED_SLAB;
        if (!state.is(mixedBlock)) return state;
        return mixedBlock.forwardSidesValue(world, pos, SlabContext::mainState, (t, a) -> {
            var towards = t.getBlock().getExplosionResistance();
            var away = a.getBlock().getExplosionResistance();
            return towards > away ? t : a;
        });
    }
}
