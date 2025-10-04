package com.dhjcomical.craftguide;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import com.dhjcomical.craftguide.client.ui.text.TranslatedTextSource;

/**
 * Implements all functionality that relies on Forge. As this class
 *  is only instantiated if Forge is actually present, this allows
 *  the rest of CraftGuide to use other loaders (so far has included
 *  pure FML, ModLoader, LiteLoader, and even direct jar insertion).
 */
public class ForgeExtensionsImplementation extends ForgeExtensions
{
    private static final TranslatedTextSource emptyOreText =
            new TranslatedTextSource("craftguide.gui.empty_oredict_type");
    @Override
    public boolean matchesTypeImpl(IRecipe recipe)
    {
        return recipe instanceof ShapedOreRecipe || recipe instanceof ShapelessOreRecipe;
    }

    @Override
    public boolean isShapelessRecipeImpl(IRecipe recipe)
    {
        return recipe instanceof ShapelessOreRecipe;
    }

    @Override
    public Object[] getCraftingRecipeImpl(RecipeGeneratorImplementation gen, IRecipe recipe, boolean allowSmallGrid)
    {
        try
        {
            if(recipe instanceof ShapedOreRecipe)
            {
                ShapedOreRecipe shaped = (ShapedOreRecipe) recipe;

                int width = shaped.getWidth();
                int height = shaped.getHeight();

                NonNullList<Ingredient> ingredients = shaped.getIngredients();

                if(allowSmallGrid && width < 3 && height < 3)
                {
                    return gen.getSmallShapedRecipe(width, height, ingredients, shaped.getRecipeOutput());
                }
                else
                {
                    return gen.getCraftingShapedRecipe(width, height, ingredients, shaped.getRecipeOutput());
                }
            }
            else if(recipe instanceof ShapelessOreRecipe)
            {
                ShapelessOreRecipe shapeless = (ShapelessOreRecipe) recipe;
                NonNullList<Ingredient> ingredients = shapeless.getIngredients();
                return gen.getCraftingShapelessRecipe(ingredients, shapeless.getRecipeOutput());
            }
        }
        catch(Exception e)
        {
            CraftGuideLog.log(e, "Error processing ore dictionary recipe", true);
        }

        return null;
    }

    private IdentityHashMap<List<?>, String> mappingCache = new IdentityHashMap<>();

    @Override
    public List<String> emptyOreDictEntryTextImpl(List<?> oreDictionaryList)
    {
        if(!mappingCache.containsKey(oreDictionaryList))
        {
            mappingCache.put(oreDictionaryList, getOreDictionaryNameImpl(oreDictionaryList));
        }

        String name = mappingCache.get(oreDictionaryList);

        if(name == null)
        {
            return null;
        }
        else
        {
            List<String> text = new ArrayList<>(1);
            text.add(emptyOreText.format(name));
            return text;
        }
    }

    private IdentityHashMap<List<?>, String> oreDictName = new IdentityHashMap<>();

    @Override
    public String getOreDictionaryNameImpl(List<?> list)
    {
        if(oreDictName.containsKey(list))
        {
            return oreDictName.get(list);
        }

        String name = getOreDictName(list);
        oreDictName.put(list, name);

        return name;
    }

    private String getOreDictName(List<?> list)
    {
        try
        {
            for (String oreName : OreDictionary.getOreNames()) {
                List<ItemStack> oreStacks = OreDictionary.getOres(oreName);
                if (oreStacks == list) {
                    return oreName;
                }
            }
        }
        catch(Exception e)
        {
            CraftGuideLog.log(e, "Error getting ore dictionary name", false);
        }

        return null;
    }
}