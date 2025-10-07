package dev.micalobia.fullslabs.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import dev.micalobia.fullslabs.block.entity.MixedSlabBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(WorldChunk.class)
public class WorldChunkMixin {
    @Definition(id = "createBlockEntity", method = "Lnet/minecraft/block/BlockEntityProvider;createBlockEntity(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Lnet/minecraft/block/entity/BlockEntity;")
    @Expression("? = ?.createBlockEntity(?, ?)")
    @ModifyVariable(method = "setBlockState", at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER))
    private BlockEntity changeMixedSlabEntity(BlockEntity entity) {
        if (!(entity instanceof MixedSlabBlockEntity mixed)) return entity;
        mixed.readCache();
        return mixed;
    }
}
