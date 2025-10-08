package com.dhjcomical.craftguide;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import javax.annotation.Nullable;
import java.util.Map;

@IFMLLoadingPlugin.Name("CraftGuideCoremod")
@IFMLLoadingPlugin.MCVersion("1.12.2")
public class CraftGuideCoremod implements IFMLLoadingPlugin {

    public CraftGuideCoremod() {

        CraftGuideLog.log("CraftGuide Coremod is loading. Initializing Mixins...");

        MixinBootstrap.init();

        Mixins.addConfiguration("mixins.craftguide.json");

        CraftGuideLog.log("Main Mixin configuration added.");
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        // Do nothing
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}