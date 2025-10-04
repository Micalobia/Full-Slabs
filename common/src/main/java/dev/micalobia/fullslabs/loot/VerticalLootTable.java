package dev.micalobia.fullslabs.loot;

import dev.architectury.event.events.common.LootEvent;
import dev.micalobia.fullslabs.block.VerticalSlabBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.BlockStatePropertyLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.ExplosionDecayLootFunction;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.registry.RegistryKey;

import java.util.Map;
import java.util.stream.Collectors;

public class VerticalLootTable implements LootEvent.ModifyLootTable {
    private Map<RegistryKey<LootTable>, SlabBlock> cache = null;

    @Override
    public void modifyLootTable(RegistryKey<LootTable> key, LootEvent.LootTableModificationContext context, boolean builtin) {
        if (!builtin) return;
        if (cache == null) {
            cache = VerticalSlabBlock.MAP_VIEW.keySet().stream()
                    .map(slab -> Map.entry(slab.getLootTableKey(), slab))
                    .filter(entry -> entry.getKey().isPresent())
                    .collect(Collectors.toMap(entry -> entry.getKey().get(), Map.Entry::getValue));
        }
        var slab = cache.get(key);
        if (slab == null) return;
        var vertical = VerticalSlabBlock.getVertical(slab);
        var pool = LootPool.builder()
                .with(ItemEntry.builder(slab))
                .conditionally(
                        BlockStatePropertyLootCondition.builder(vertical).properties(StatePredicate.Builder.create().exactMatch(VerticalSlabBlock.TYPE, VerticalSlabBlock.VerticalType.FULL))
                )
                .apply(ExplosionDecayLootFunction.builder());
        context.addPool(pool);
    }
}
