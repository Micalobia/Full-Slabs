package dev.micalobia.fullslabs.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.micalobia.fullslabs.FullSlabs;
import eu.midnightdust.lib.config.MidnightConfig;

public class FullSlabsModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> MidnightConfig.getScreen(parent, FullSlabs.MODID);
    }
}
