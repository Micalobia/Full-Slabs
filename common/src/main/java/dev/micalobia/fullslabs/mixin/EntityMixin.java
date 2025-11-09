package dev.micalobia.fullslabs.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import dev.micalobia.fullslabs.SlabRegistry;
import dev.micalobia.fullslabs.block.entity.MixedSlabBlockEntity;
import dev.micalobia.fullslabs.ducks.EntityDuck;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Entity.class)
public class EntityMixin implements EntityDuck {
    @Definition(id = "walkingStepSound", method = "Lnet/minecraft/world/entity/Entity;walkingStepSound(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V")
    @Expression("?.walkingStepSound(?, ?)")
    @ModifyArg(method = "vibrationAndSoundEffectsFromBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;ZZLnet/minecraft/world/phys/Vec3;)Z", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    private BlockState mixedSlabStepSounds(BlockState state, @Local(argsOnly = true) BlockPos pos) {
        return fullslabs$tryGetMixedState(state, pos);
    }

    public BlockState fullslabs$tryGetMixedState(BlockState state, BlockPos pos) {
        var mixed = SlabRegistry.MIXED_SLAB.get();
        if (!(state.is(mixed))) return state;
        var self = (Entity) (Object) this;
        var blockEntity = self.level().getBlockEntity(pos);
        if (!(blockEntity instanceof MixedSlabBlockEntity mixedEntity)) return state;
        return mixedEntity.getState(mixed.towards(state, self.position(), pos));
    }
}
