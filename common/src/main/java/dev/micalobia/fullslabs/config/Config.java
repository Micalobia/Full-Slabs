package dev.micalobia.fullslabs.config;


import dev.isxander.yacl3.config.v2.api.SerialEntry;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;

import java.util.ArrayList;
import java.util.List;

public class Config {
    @SerialEntry
    public List<String> tiltedSlabs = new ArrayList<>();

    public boolean isTilted(Block block) {
        var id = Registries.BLOCK.getId(block);
        return tiltedSlabs.contains(id.toString());
    }
}
