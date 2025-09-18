package dev.micalobia.fullslabs.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.micalobia.fullslabs.config.ConfigManager;

public class FullSlabsModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ConfigManager::createScreen;
    }
}
