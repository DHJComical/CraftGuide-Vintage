package com.dhjcomical.craftguide;

import java.io.File;

import com.dhjcomical.gui_craftguide.theme.ThemeManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

@Mod(modid = "craftguide", name = "CraftGuide", version = "1.7.0")
public class CraftGuide_FML implements CraftGuideLoaderSide {

    public static Logger logger;

    @SidedProxy(clientSide = "com.dhjcomical.craftguide.client.fml.CraftGuideClient_FML",
            serverSide = "com.dhjcomical.craftguide.server.CraftGuideServer")
    public static CraftGuideSide side;

    private CraftGuide craftguide;

    private static File configDir;

    public static class KeyCheckTick {
        @SubscribeEvent
        public void clientTick(TickEvent.ClientTickEvent event) {
            side.checkKeybind();
        }
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();

        configDir = event.getModConfigurationDirectory();

        CraftGuide.loaderSide = this;
        CraftGuide.side = side;

        craftguide = new CraftGuide();

        craftguide.preInit(false);

        MinecraftForge.EVENT_BUS.register(new KeyCheckTick());
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        craftguide.init();
        if (ThemeManager.currentThemeName != null && !ThemeManager.currentThemeName.isEmpty()) {
            ThemeManager.currentTheme = ThemeManager.instance.buildTheme(ThemeManager.currentThemeName);

            if (ThemeManager.currentTheme == null) {
                CraftGuideLog.log("Failed to build theme: " + ThemeManager.currentThemeName);
            }
        }
    }


    @Override
    public boolean isModLoaded(String name) {
        return Loader.isModLoaded(name);
    }

    @Override
    public File getConfigDir() {
        return configDir;
    }

    @Override
    public File getLogDir() {
        return new File(configDir.getParentFile().getParentFile(), "logs");
    }

    @Override
    public void addRecipe(ItemStack output, Object[] recipe) {
        ResourceLocation name = output.getItem().getRegistryName();
        if (name == null) {
            return;
        }
        GameRegistry.addShapedRecipe(name, null, output, recipe);
    }

    @Override
    public void logConsole(String text) {
        logger.log(Level.INFO, text);
    }

    @Override
    public void logConsole(String text, Throwable e) {
        logger.log(Level.WARN, text, e);
    }

    @Override
    public void initClientNetworkChannels() {
    }
}