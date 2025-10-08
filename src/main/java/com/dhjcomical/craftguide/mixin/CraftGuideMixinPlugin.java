package com.dhjcomical.craftguide.mixin;

import net.minecraftforge.fml.common.Loader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class CraftGuideMixinPlugin implements IMixinConfigPlugin {


    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        String[] parts = mixinClassName.split("\\.");
        if (parts.length < 5) {
            return true;
        }

        String category = parts[4];

        switch (category) {
            case "ic2exp":
                try {
                    Class.forName("ic2.core.recipe.AdvRecipe");
                    return Loader.isModLoaded("ic2");
                } catch (ClassNotFoundException e) {
                    return false;
                }

            case "ic2classic":
                try {
                    Class.forName("ic2.core.item.recipe.AdvRecipe");
                    return Loader.isModLoaded("ic2");
                } catch (ClassNotFoundException e) {
                    return false;
                }

            default:
                return true;
        }
    }


    @Override
    public void onLoad(String mixinPackage) { }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) { }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) { }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) { }
}