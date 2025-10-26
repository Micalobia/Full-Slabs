package dev.micalobia.fullslabs;

import dev.architectury.platform.Platform;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public final class FullSlabsMixinPlugin implements IMixinConfigPlugin {
    // Takes a class name of the form *.compat.modid.* and extracts the mod id. Used to make sure the mixin is only loaded when the mod is
    public static final Pattern COMPAT_REGEX = Pattern.compile(".*compat\\.(?<modid>\\w+)\\..*");

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        var match = COMPAT_REGEX.matcher(mixinClassName);
        if (match.matches()) return Platform.isModLoaded(match.group("modid"));
        return true;
    }

    // Boilerplate

    @Override
    public void onLoad(String s) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> set, Set<String> set1) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {

    }

    @Override
    public void postApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {

    }
}
