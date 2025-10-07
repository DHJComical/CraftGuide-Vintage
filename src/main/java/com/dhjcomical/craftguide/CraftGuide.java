package com.dhjcomical.craftguide;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList; // <-- IMPORT
import java.util.HashMap;
import java.util.List; // <-- IMPORT
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import com.dhjcomical.craftguide.api.RecipeProvider; // <-- IMPORT
import com.dhjcomical.craftguide.recipes.*; // <-- IMPORT all recipe providers
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;
import com.dhjcomical.craftguide.api.ItemSlot;
import com.dhjcomical.gui_craftguide.theme.ThemeManager;

@SuppressWarnings("deprecation")
public class CraftGuide
{
    public static CraftGuideSide side;
    public static CraftGuideLoaderSide loaderSide;

    private static Properties config = new Properties();
    private static Map<String, String> configComments;

    public static int resizeRate;
    public static int mouseWheelScrollRate;
    public static int defaultKeybind;
    public static boolean pauseWhileOpen = true;
    public static boolean gridPacking = true;
    public static boolean alwaysShowID = false;
    public static boolean textSearchRequiresShift = false;
    public static boolean enableKeybind = true;
    public static boolean newerBackgroundStyle = false;
    public static boolean hideMundanePotionRecipes = true;
    public static boolean insertBetterWithRenewablesRecipes = false;
    public static boolean enableItemRecipe = true;
    public static boolean rightClickClearText = true;
    public static boolean betterWithRenewablesDetected = false;
    public static boolean needsRecipeRefresh = false;
    public static boolean ae2Workaround = true;
    public static boolean useWorkerThread = true;
    public static final int DAMAGE_WILDCARD = 32767;
    public static ItemCraftGuide itemCraftGuide = (ItemCraftGuide) ModItems.CRAFT_GUIDE;

    public void preInit(boolean disableItem)
    {
        initForgeExtensions();
        loadProperties();
        addItem();
        side.preInit();
        ItemSlot.implementation = new ItemSlotImplementationImplementation();
        com.dhjcomical.craftguide.api.slotTypes.ItemSlot.implementation = new ItemSlotImplementationImplementation();
        side.initKeybind();
    }

    public void init()
    {
        RecipeGeneratorImplementation generator = RecipeGeneratorImplementation.instance;

        generator.addProvider(new DefaultRecipeProvider());
        generator.addProvider(new BrewingRecipes());
        generator.addProvider(new GrassSeedDrops());

        if (loaderSide.isModLoaded("ic2")) {
            try {
                generator.addProvider(new IC2ExperimentalRecipes());
            } catch (Throwable e) {
                CraftGuideLog.log("Failed to initialize IC2 recipe provider. IC2 recipes will not be available.");
                CraftGuideLog.log(e);
            }
        }

        if (loaderSide.isModLoaded("buildcraftfactory")) {
            try {
                Object provider = Class.forName("com.dhjcomical.craftguide.recipes.BuildCraftRecipes").newInstance();
                if (provider instanceof RecipeProvider) {
                    generator.addProvider((RecipeProvider) provider);
                }
            } catch (Exception e) {
                CraftGuideLog.log("Failed to load recipe provider for BuildCraft", true);
            }
        }

        side.initNetworkChannels();
    }


    private void initForgeExtensions()
    {
        if(loaderSide.isModLoaded("Forge"))
        {
            try
            {
                ForgeExtensions.setImplementation((ForgeExtensions)Class.forName("com.dhjcomical.craftguide.ForgeExtensionsImplementation").newInstance());
            }
            catch(InstantiationException | IllegalAccessException | ClassNotFoundException e)
            {
                CraftGuideLog.log(e);
            }
        }
    }

    private void addItem()
    {
        if(enableItemRecipe)
        {
            loaderSide.addRecipe(new ItemStack(itemCraftGuide), new Object[] {"pbp",
                    "bcb", "pbp", Character.valueOf('c'), Blocks.CRAFTING_TABLE,
                    Character.valueOf('p'), Items.PAPER, Character.valueOf('b'),
                    Items.BOOK});
        }
    }

    static
    {
        configComments = new HashMap<>();
        configComments.put("newerBackgroundStyle", "If false, CraftGuide will use the images from craftguiderecipe.png for vanilla shaped crafting recipes, which is better for texture packs. If true, CraftGuide will use the default background (pieced together from parts of craftguide.png, then slot backgrounds drawn overtop), which is worse for texture packs, and looks identical without a texture pack.");
        configComments.put("hideMundanePotionRecipes", "Hides recipes that convert a useful potion into a mundane potion with the damage value 8192, which is basically a failed potion. In the vanilla ingredients, they only occur when you try to add an effect without first adding netherwart (or add a second effect ingredient without using netherwart in between). Note that 8192 means, to the vanilla brewing system 'can make a splash potion without losing it's effects', and a potion with a value of EXACTLY 8192 does not have any effects anyway.");
        configComments.put("logThemeDebugInfo", "If true, CraftGuide will output a lot of debugging text every time it reloads the themes.");
        configComments.put("gridPacking", "Affects whether CraftGuide distributes leftover horizontal space between columns, or puts it all at the far right. Currently not useful, as any grid with columns now resizes itself so that it doesn't have any leftover horizntal space that needs to be distributed.");
        configComments.put("resizeRate", "If greater than 0, the maximum number of pixels that the CraftGuide window will change size by each frame. When the effect was actually tried in-game, it just made the GUI feel slow, so defaults to 0 ('ALL the pixels!').");
        configComments.put("textSearchRequiresShift", "Normally, when typing in the item list search box, pressing enter instantly returns to the recipe list, using whatever you had typed as the text filter. If this option is true, you also have to hold shift, to avoid accidentally searching.");
        configComments.put("RecipeList_mouseWheelScrollRate", "How many rows to scroll for each unit of mouse wheel scrolliness.");
        configComments.put("enableItemRecipe", "Whether you can craft the CraftGuide item.");
        configComments.put("enableKeybind", "Whether CraftGuide sets up a keybind so that you can open it without the item.");
        configComments.put("PauseWhileOpen", "In singleplayer, whether the game is paused while you have CraftGuide open. If false, you can browse recipes while waiting for your machines to run, but it also means that a ninja creeper may be able to sneak up behind you while you are distracted.");
        configComments.put("alwaysShowID", "If true, item tooltips have an additional line showing their item ID and damage value. Added before the vanilla F3+H, it has a different format, and puts the item ID on a separate line from the item name. If this setting is false, CraftGuide will only show item IDs in this way in the rare case of an item error");
        configComments.put("defaultKeybind", "If Minecraft isn't properly loading changed keybinds, or you are putting together a config/modpack and want a different default value, you can change the default CraftGuide keybind here.");
        configComments.put("rightClickClearText", "Right-clicking a text input clears it, instead of setting the cursor position.");
        configComments.put("ae2Workaround", "Workaround for slow startup time and exception spam with some AE2 versions.");
        configComments.put("useWorkerThread", "Run potentially slow tasks (such as constructing the recipe list, or performing searches) in a separate thread. Will keep the game responsive, but may cause stability issues.");
    }

    private void setConfigDefaults()
    {
        config.setProperty("RecipeList_mouseWheelScrollRate", "3");
        config.setProperty("PauseWhileOpen", Boolean.toString(true));
        config.setProperty("resizeRate", "0");
        config.setProperty("gridPacking", Boolean.toString(true));
        config.setProperty("alwaysShowID", Boolean.toString(false));
        config.setProperty("textSearchRequiresShift", Boolean.toString(false));
        config.setProperty("enableKeybind", Boolean.toString(true));
        config.setProperty("enableItemRecipe", Boolean.toString(true));
        config.setProperty("newerBackgroundStyle", Boolean.toString(false));
        config.setProperty("hideMundanePotionRecipes", Boolean.toString(true));
        config.setProperty("insertBetterWithRenewablesRecipes", Boolean.toString(false));
        config.setProperty("logThemeDebugInfo", Boolean.toString(false));
        config.setProperty("rightClickClearText", Boolean.toString(true));
        config.setProperty("defaultKeybind", Integer.toString(Keyboard.KEY_G));
        config.setProperty("ae2Workaround", Boolean.toString(true));
        config.setProperty("useWorkerThread", Boolean.toString(true));
        config.setProperty("theme", "base");
    }

    private void loadProperties()
    {
        File oldConfigDir = loaderSide.getConfigDir();
        File oldConfigFile = new File(oldConfigDir, "CraftGuide.cfg");
        File newConfigDir = configDirectory();
        File newConfigFile = newConfigDir == null? null : new File(newConfigDir, "CraftGuide.cfg");
        File configFile = null;

        if(newConfigFile != null && newConfigFile.exists())
        {
            configFile = newConfigFile;
        }
        else if(oldConfigFile.exists() && oldConfigFile.canRead())
        {
            configFile = oldConfigFile;
        }

        setConfigDefaults();

        if(configFile != null && configFile.exists() && configFile.canRead())
        {
            try(FileInputStream inStream = new FileInputStream(configFile))
            {
                config.load(inStream);
            }
            catch(IOException e)
            {
                CraftGuideLog.log(e, "", true);
            }
        }

        try { resizeRate = Integer.valueOf(config.getProperty("resizeRate")); } catch(NumberFormatException e) { }
        try { mouseWheelScrollRate = Integer.valueOf(config.getProperty("RecipeList_mouseWheelScrollRate")); } catch(NumberFormatException e) { }
        try { defaultKeybind = Integer.valueOf(config.getProperty("defaultKeybind")); } catch(NumberFormatException e) { }
        pauseWhileOpen = Boolean.valueOf(config.getProperty("PauseWhileOpen"));
        gridPacking = Boolean.valueOf(config.getProperty("gridPacking"));
        alwaysShowID = Boolean.valueOf(config.getProperty("alwaysShowID"));
        textSearchRequiresShift = Boolean.valueOf(config.getProperty("textSearchRequiresShift"));
        enableKeybind = Boolean.valueOf(config.getProperty("enableKeybind"));
        newerBackgroundStyle = Boolean.valueOf(config.getProperty("newerBackgroundStyle"));
        hideMundanePotionRecipes = Boolean.valueOf(config.getProperty("hideMundanePotionRecipes"));
        insertBetterWithRenewablesRecipes = Boolean.valueOf(config.getProperty("insertBetterWithRenewablesRecipes"));
        ThemeManager.debugOutput = Boolean.valueOf(config.getProperty("logThemeDebugInfo"));
        enableItemRecipe = Boolean.valueOf(config.getProperty("enableItemRecipe"));
        rightClickClearText = Boolean.valueOf(config.getProperty("rightClickClearText"));
        ae2Workaround = Boolean.valueOf(config.getProperty("ae2Workaround"));
        useWorkerThread = Boolean.valueOf(config.getProperty("useWorkerThread"));
        ThemeManager.currentThemeName = config.getProperty("theme", "base");

        if(newConfigFile != null && !newConfigFile.exists()) { try { newConfigFile.createNewFile(); } catch(IOException e) { CraftGuideLog.log(e, "", true); } }
        if(newConfigFile != null && newConfigFile.exists() && newConfigFile.canWrite()) { try(FileOutputStream outputStream = new FileOutputStream(newConfigFile)) { saveConfig(outputStream); } catch(IOException e) { CraftGuideLog.log(e, "", true); } }
    }

    private static void saveConfig(OutputStream outputStream) throws IOException
    {
        SortedSet<String> properties = new TreeSet<>(config.stringPropertyNames());
        try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
            for(String property: properties) {
                if(configComments.containsKey(property)) {
                    writer.newLine();
                    writer.write("# ");
                    writer.write(configComments.get(property));
                    writer.newLine();
                    writer.write(property);
                    writer.write('=');
                    writer.write(config.getProperty(property));
                    writer.newLine();
                }
            }
            writer.newLine();
            for(String property: properties) {
                if(!configComments.containsKey(property)) {
                    writer.write(property);
                    writer.write('=');
                    writer.write(config.getProperty(property));
                    writer.newLine();
                }
            }
        }
    }

    public static File configDirectory()
    {
        File dir = new File(loaderSide.getConfigDir(), "CraftGuide");
        if(!dir.exists() && !dir.mkdirs()) return null;
        return dir;
    }

    public static File logDirectory() { return loaderSide.getLogDir(); }

    public static void saveConfig()
    {
        config.setProperty("PauseWhileOpen", Boolean.toString(pauseWhileOpen));
        config.setProperty("alwaysShowID", Boolean.toString(alwaysShowID));
        config.setProperty("enableKeybind", Boolean.toString(enableKeybind));
        config.setProperty("enableItemRecipe", Boolean.toString(enableItemRecipe));
        config.setProperty("hideMundanePotionRecipes", Boolean.toString(hideMundanePotionRecipes));
        config.setProperty("logThemeDebugInfo", Boolean.toString(ThemeManager.debugOutput));
        config.setProperty("rightClickClearText", Boolean.toString(rightClickClearText));
        config.setProperty("ae2Workaround", Boolean.toString(ae2Workaround));
        config.setProperty("useWorkerThread", Boolean.toString(useWorkerThread));
        try(FileOutputStream outputStream = new FileOutputStream(new File(configDirectory(), "CraftGuide.cfg"))) {
            saveConfig(outputStream);
        } catch(IOException e) {
            CraftGuideLog.log(e, "", true);
        }
    }

    @Deprecated
    public static RuntimeException unimplemented()
    {
        return new RuntimeException("Unimplemented");
    }
}