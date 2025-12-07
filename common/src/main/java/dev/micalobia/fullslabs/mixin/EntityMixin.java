package dev.micalobia.fullslabs.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import dev.micalobia.fullslabs.block.SlabLike;
import dev.micalobia.fullslabs.ducks.EntityDuck;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Entity.class)
public class EntityMixin implements EntityDuck {
    @Shadow
    private Level level;

    @Shadow
    private Vec3 position;

    @Definition(id = "walkingStepSound", method = "Lnet/minecraft/world/entity/Entity;walkingStepSound(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V")
    @Expression("?.walkingStepSound(?, ?)")
    @ModifyArg(method = "vibrationAndSoundEffectsFromBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;ZZLnet/minecraft/world/phys/Vec3;)Z", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    private BlockState mixedSlabStepSounds(BlockState state, @Local(argsOnly = true) BlockPos pos) {
        return fullslabs$tryGetMixedState(state, pos);
    }

    public BlockState fullslabs$tryGetMixedState(BlockState state, BlockPos pos) {
        if (!(state.getBlock() instanceof SlabLike slab) || slab.isUnmixed()) return state;
        return slab.getHalf(state, this.level, pos, slab.isHitTowards(state, pos, this.position));
    }
}
