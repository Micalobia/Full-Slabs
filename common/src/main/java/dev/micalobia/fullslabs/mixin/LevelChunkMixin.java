package dev.micalobia.fullslabs.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import dev.micalobia.fullslabs.block.entity.MixedSlabBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LevelChunk.class)
public class LevelChunkMixin {
    @Definition(id = "newBlockEntity", method = "Lnet/minecraft/world/level/block/EntityBlock;newBlockEntity(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/level/block/entity/BlockEntity;")
    @Expression("? = ?.newBlockEntity(?, ?)")
    @ModifyVariable(method = "setBlockState", at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER))
    private BlockEntity changeMixedSlabEntity(BlockEntity entity) {
        if (!(entity instanceof MixedSlabBlockEntity mixed)) return entity;
        mixed.readCache();
        return mixed;
    }
}
