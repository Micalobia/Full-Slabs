package dev.micalobia.fullslabs.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Set;

@Mixin(BlockEntityType.class)
public interface BlockEntityTypeAccessor {
    @Invoker("<init>")
    static <T extends BlockEntity> BlockEntityType<T> constructor(BlockEntityType.BlockEntityFactory<? extends T> factory, Set<Block> blocks) {
        throw new AssertionError();
    }
}
