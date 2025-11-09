package dev.micalobia.fullslabs.mixin;

import dev.micalobia.fullslabs.SlabRegistry;
import dev.micalobia.fullslabs.block.entity.MixedSlabBlockEntity;
import dev.micalobia.fullslabs.ducks.LivingEntityDuck;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LivingEntity.class)
public class LivingEntityMixin implements LivingEntityDuck {
    @Override
    public BlockState fullslabs$getMixedLandingState(BlockState state, BlockPos landedPosition) {
        var mixed = SlabRegistry.MIXED_SLAB.get();
        if (!(state.is(mixed))) return state;
        var self = (LivingEntity) (Object) this;
        var blockEntity = self.level().getBlockEntity(landedPosition);
        if (!(blockEntity instanceof MixedSlabBlockEntity mixedEntity)) return state;
        return mixedEntity.getState(mixed.towards(state, self.position(), landedPosition));
    }
}
