package dev.micalobia.fullslabs.loot;

import dev.architectury.event.events.common.LootEvent;
import dev.micalobia.fullslabs.FullSlabs;
import dev.micalobia.fullslabs.block.VerticalSlabBlock;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class VerticalLootTable implements LootEvent.ModifyLootTable {
    private Map<ResourceKey<LootTable>, SlabBlock> cache = null;

    @Override
    public void modifyLootTable(ResourceKey<LootTable> key, LootEvent.LootTableModificationContext context, boolean builtin) {
        if (!builtin) return;
        if (cache == null) {
            var grouped = VerticalSlabBlock.MAP_VIEW.keySet().stream()
                    .map(slab -> Map.entry(slab.getLootTable(), slab))
                    .filter(entry -> entry.getKey().isPresent())
                    .collect(Collectors.groupingBy(entry -> entry.getKey().get(), Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
            cache = new HashMap<>();
            grouped.forEach((lootKey, slabs) -> {
                if (slabs.size() == 1) cache.put(lootKey, slabs.getFirst());
                else {
                    FullSlabs.LOGGER.warn(
                            "Loot table {} is shared by {} slabs; {} - skipping",
                            lootKey,
                            slabs.size(),
                            slabs.stream()
                                    .map(BuiltInRegistries.BLOCK::getKey)
                                    .map(ResourceLocation::toString)
                                    .collect(Collectors.joining(", ")));
                }
            });
        }
        var slab = cache.get(key);
        if (slab == null) return;
        var vertical = VerticalSlabBlock.getVertical(slab);
        var pool = LootPool.lootPool()
                .add(LootItem.lootTableItem(slab))
                .when(
                        LootItemBlockStatePropertyCondition.hasBlockStateProperties(vertical).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(VerticalSlabBlock.TYPE, VerticalSlabBlock.VerticalType.FULL))
                )
                .apply(ApplyExplosionDecay.explosionDecay());
        context.addPool(pool);
    }
}
