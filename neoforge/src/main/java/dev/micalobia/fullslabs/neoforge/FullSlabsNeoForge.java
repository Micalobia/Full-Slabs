package dev.micalobia.fullslabs.neoforge;

import dev.micalobia.fullslabs.FullSlabs;
import dev.micalobia.fullslabs.block.entity.MixedSlabBlockEntity;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.model.data.ModelProperty;

@Mod(FullSlabs.MODID)
public final class FullSlabsNeoForge {
    public static final ModelProperty<MixedSlabBlockEntity.ModelContext> MIXED_CONTEXT_MODEL_PROPERTY = new ModelProperty<>();

    public FullSlabsNeoForge() {
        FullSlabs.init();
    }
}
