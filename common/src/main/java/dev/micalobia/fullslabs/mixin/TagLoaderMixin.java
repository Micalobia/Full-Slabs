package dev.micalobia.fullslabs.mixin;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.micalobia.fullslabs.block.VerticalSlabBlock;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagLoader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.Map;

@Mixin(TagLoader.class)
public class TagLoaderMixin {
    @Shadow
    @Final
    private String directory;

    @ModifyReturnValue(method = "build", at = @At("RETURN"))
    private Map<ResourceLocation, List<Holder.Reference<Block>>> injectVerticalSlabTags(Map<ResourceLocation, List<Holder.Reference<Block>>> original) {
        if (!"tags/block".equals(this.directory)) return original;
        original.keySet().stream().filter(id -> id.getPath().contains("mineable/")).forEach(id -> {
            var list = original.get(id);
            var slabs = list.stream()
                    .map(Holder.Reference::value)
                    .filter(SlabBlock.class::isInstance)
                    .map(SlabBlock.class::cast)
                    .filter(VerticalSlabBlock::hasVertical)
                    .map(VerticalSlabBlock::getVertical)
                    .map(block -> (Holder.Reference<Block>) BuiltInRegistries.BLOCK.wrapAsHolder(block))
                    .toList();
            if (!slabs.isEmpty()) {
                // This changes the type from `ImmutableCollections$ListN` to `RegularImmutableList`
                // It's only expecting a `List`, so this should be fine
                var updated = ImmutableList.<Holder.Reference<Block>>builderWithExpectedSize(list.size() + slabs.size())
                        .addAll(list)
                        .addAll(slabs)
                        .build();
                original.put(id, updated);
            }
        });
        return original;
    }
}
