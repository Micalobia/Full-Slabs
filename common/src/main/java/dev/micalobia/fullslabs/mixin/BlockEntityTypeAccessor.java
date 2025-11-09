package dev.micalobia.fullslabs.mixin;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Set;

@Mixin(BlockEntityType.class)
public interface BlockEntityTypeAccessor {
    @Invoker("<init>")
    static <T extends BlockEntity> BlockEntityType<T> constructor(BlockEntityType.BlockEntitySupplier<? extends T> factory, Set<Block> blocks) {
        throw new AssertionError();
    }
}
