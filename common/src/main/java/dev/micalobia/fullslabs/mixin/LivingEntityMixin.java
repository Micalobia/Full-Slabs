package dev.micalobia.fullslabs.mixin;

import dev.micalobia.fullslabs.block.SlabLike;
import dev.micalobia.fullslabs.ducks.LivingEntityDuck;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LivingEntity.class)
public class LivingEntityMixin implements LivingEntityDuck {
    @Override
    public BlockState fullslabs$getMixedLandingState(BlockState state, BlockPos landedPosition) {
        if (!(state.getBlock() instanceof SlabLike slab) || slab.isUnmixed()) return state;
        var self = (LivingEntity) (Object) this;
        return slab.getHalf(state, self.level(), landedPosition, slab.isHitTowards(state, landedPosition, self.position()));
    }
}
