package dev.micalobia.fullslabs.loot;

import dev.architectury.event.events.common.LootEvent;
import dev.micalobia.fullslabs.block.VerticalSlabBlock;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;

import java.util.Map;
import java.util.stream.Collectors;

public class VerticalLootTable implements LootEvent.ModifyLootTable {
    private Map<ResourceKey<LootTable>, SlabBlock> cache = null;

    @Override
    public void modifyLootTable(ResourceKey<LootTable> key, LootEvent.LootTableModificationContext context, boolean builtin) {
        if (!builtin) return;
        if (cache == null) {
            cache = VerticalSlabBlock.MAP_VIEW.keySet().stream()
                    .map(slab -> Map.entry(slab.getLootTable(), slab))
                    .filter(entry -> entry.getKey().isPresent())
                    .collect(Collectors.toMap(entry -> entry.getKey().get(), Map.Entry::getValue));
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
