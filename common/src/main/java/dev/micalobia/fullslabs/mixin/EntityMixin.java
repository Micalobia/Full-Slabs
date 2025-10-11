package dev.micalobia.fullslabs.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import dev.micalobia.fullslabs.SlabRegistry;
import dev.micalobia.fullslabs.block.entity.MixedSlabBlockEntity;
import dev.micalobia.fullslabs.ducks.EntityDuck;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Entity.class)
public class EntityMixin implements EntityDuck {
    @Definition(id = "playStepSounds", method = "Lnet/minecraft/entity/Entity;playStepSounds(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V")
    @Expression("?.playStepSounds(?, ?)")
    @ModifyArg(method = "stepOnBlock", at = @At("MIXINEXTRAS:EXPRESSION"))
    private BlockState mixedSlabStepSounds(BlockState state, @Local(argsOnly = true) BlockPos pos) {
        return fullslabs$tryGetMixedState(state, pos);
    }

    public BlockState fullslabs$tryGetMixedState(BlockState state, BlockPos pos) {
        var mixed = SlabRegistry.MIXED_SLAB.get();
        if (!(state.isOf(mixed))) return state;
        var self = (Entity) (Object) this;
        var blockEntity = self.getWorld().getBlockEntity(pos);
        if (!(blockEntity instanceof MixedSlabBlockEntity mixedEntity)) return state;
        return mixedEntity.getState(mixed.towards(state, self.getPos(), pos));
    }
}
