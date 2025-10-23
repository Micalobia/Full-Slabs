package dev.micalobia.fullslabs.fabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import dev.micalobia.fullslabs.SlabRegistry;
import dev.micalobia.fullslabs.handlers.MixedContext;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ExplosionBehavior.class)
public class ExplosionBehaviorMixin {
    @ModifyReceiver(method = "getBlastResistance", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getBlock()Lnet/minecraft/block/Block;"))
    private BlockState mixedSlabBlastResistence(BlockState state, Explosion explosion, BlockView world, BlockPos pos) {
        var mixedBlock = SlabRegistry.MIXED_SLAB.get();
        if (!state.isOf(mixedBlock)) return state;
        return mixedBlock.forwardSidesValue(world, pos, MixedContext.Sided::state, (t, a) -> {
            var towards = t.getBlock().getBlastResistance();
            var away = a.getBlock().getBlastResistance();
            return towards > away ? t : a;
        });
    }
}
