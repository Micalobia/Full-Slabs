package dev.micalobia.fullslabs.mixin;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.micalobia.fullslabs.block.VerticalSlabBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SlabBlock;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagGroupLoader;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.Map;

@Mixin(TagGroupLoader.class)
public class TagGroupLoaderMixin {
    @Shadow
    @Final
    private String dataType;

    @ModifyReturnValue(method = "buildGroup", at = @At("RETURN"))
    private Map<Identifier, List<RegistryEntry.Reference<Block>>> injectVerticalSlabTags(Map<Identifier, List<RegistryEntry.Reference<Block>>> original) {
        if (!"tags/block".equals(this.dataType)) return original;
        original.keySet().stream().filter(id -> id.getPath().contains("mineable/")).forEach(id -> {
            var list = original.get(id);
            var slabs = list.stream()
                    .map(RegistryEntry.Reference::value)
                    .filter(SlabBlock.class::isInstance)
                    .map(SlabBlock.class::cast)
                    .filter(VerticalSlabBlock::hasVertical)
                    .map(VerticalSlabBlock::getVertical)
                    .map(block -> (RegistryEntry.Reference<Block>) Registries.BLOCK.getEntry(block))
                    .toList();
            if (!slabs.isEmpty()) {
                // This changes the type from `ImmutableCollections$ListN` to `RegularImmutableList`
                // It's only expecting a `List`, so this should be fine
                var updated = ImmutableList.<RegistryEntry.Reference<Block>>builderWithExpectedSize(list.size() + slabs.size())
                        .addAll(list)
                        .addAll(slabs)
                        .build();
                original.put(id, updated);
            }
        });
        return original;
    }
}
