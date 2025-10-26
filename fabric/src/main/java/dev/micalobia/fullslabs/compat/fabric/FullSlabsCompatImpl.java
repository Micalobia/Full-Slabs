package dev.micalobia.fullslabs.compat.fabric;

import com.brand.blockus.blocks.base.amethyst.AmethystSlabBlock;
import com.brand.blockus.blocks.base.asphalt.AsphaltSlab;
import com.brand.blockus.blocks.base.redstone.RedstoneSlabBlock;
import dev.architectury.platform.Mod;
import dev.architectury.platform.Platform;
import dev.micalobia.fullslabs.SlabRegistry;
import dev.micalobia.fullslabs.fabric.compat.blockus.AmethystVerticalSlabBlock;
import dev.micalobia.fullslabs.fabric.compat.blockus.AsphaltVerticalSlabBlock;
import dev.micalobia.fullslabs.fabric.compat.blockus.RedstoneVerticalSlabBlock;
import dev.micalobia.fullslabs.fabric.compat.mo_glass.GlassVerticalSlabBlock;
import dev.micalobia.fullslabs.fabric.compat.mo_glass.StainedGlassVerticalSlabBlock;
import dev.micalobia.fullslabs.fabric.compat.mo_glass.TintedGlassVerticalSlabBlock;
import dev.micalobia.fullslabs.handlers.BlindProjectileHitHandler;
import dev.micalobia.fullslabs.handlers.BlindSteppedOnHandler;
import dev.micalobia.fullslabs.handlers.MixedHandlers;
import dev.micalobia.fullslabs.handlers.RedstoneMixedHandler;
import net.wurstclient.glass.GlassSlabBlock;
import net.wurstclient.glass.StainedGlassSlabBlock;
import net.wurstclient.glass.TintedGlassSlabBlock;

public final class FullSlabsCompatImpl {
    public static void platformInit() {
        Platform.getOptionalMod("blockus").ifPresent(FullSlabsCompatImpl::blockus);
        Platform.getOptionalMod("mo_glass").ifPresent(FullSlabsCompatImpl::mo_glass);
    }

    public static void blockus(Mod mod) {
        SlabRegistry.registerVertical(RedstoneSlabBlock.class, RedstoneVerticalSlabBlock::new);
        MixedHandlers.register(RedstoneSlabBlock.class, new RedstoneMixedHandler(15, 0));
        SlabRegistry.registerVertical(AmethystSlabBlock.class, AmethystVerticalSlabBlock::new);
        MixedHandlers.register(AmethystSlabBlock.class, BlindProjectileHitHandler.INSTANCE);
        SlabRegistry.registerVertical(AsphaltSlab.class, AsphaltVerticalSlabBlock::new);
        MixedHandlers.register(AsphaltSlab.class, BlindSteppedOnHandler.INSTANCE);
    }

    public static void mo_glass(Mod mod) {
        SlabRegistry.registerVertical(GlassSlabBlock.class, GlassVerticalSlabBlock::new);
        SlabRegistry.registerVertical(StainedGlassSlabBlock.class, StainedGlassVerticalSlabBlock::new);
        SlabRegistry.registerVertical(TintedGlassSlabBlock.class, TintedGlassVerticalSlabBlock::new);
    }
}
