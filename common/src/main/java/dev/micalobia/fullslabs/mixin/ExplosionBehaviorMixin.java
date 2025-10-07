package dev.micalobia.fullslabs.mixin;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import dev.micalobia.fullslabs.SlabRegistry;
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
        return mixedBlock.act(world, pos, mixed -> {
            var towards = mixed.getTowardsState();
            var away = mixed.getAwayState();
            var towardsStronger = towards.getBlock().getBlastResistance() > away.getBlock().getBlastResistance();
            return towardsStronger ? towards : away;
        });
    }
}
