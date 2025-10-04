package com.dhjcomical.craftguide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.NonNullList;
import com.dhjcomical.craftguide.api.CraftGuideRecipe;
import com.dhjcomical.craftguide.api.RecipeGenerator;
import com.dhjcomical.craftguide.api.RecipeTemplate;
import com.dhjcomical.craftguide.api.RecipeTemplateBuilder;
import com.dhjcomical.craftguide.api.Slot;
import com.dhjcomical.craftguide.template_builder.RecipeTemplateBuilderImplementation;
import com.dhjcomical.gui_craftguide.minecraft.Image;
import com.dhjcomical.gui_craftguide.texture.BlankTexture;
import com.dhjcomical.gui_craftguide.texture.BorderedTexture;
import com.dhjcomical.gui_craftguide.texture.DynamicTexture;
import com.dhjcomical.gui_craftguide.texture.SubTexture;
import com.dhjcomical.gui_craftguide.texture.Texture;
import com.dhjcomical.gui_craftguide.texture.TextureClip;

public class RecipeGeneratorImplementation implements RecipeGenerator
{
    private Map<ItemStack, List<CraftGuideRecipe>> recipes = new HashMap<>();
    public List<ItemStack> disabledTypes = new LinkedList<>();
    public Texture defaultBackground = new BlankTexture();
    public Texture defaultBackgroundSelected;
    public static ItemStack workbench = new ItemStack(Blocks.CRAFTING_TABLE);
    public static final RecipeGeneratorImplementation instance = new RecipeGeneratorImplementation();

    private RecipeGeneratorImplementation()
    {
        Texture source = DynamicTexture.instance("base_image");
        defaultBackgroundSelected = new BorderedTexture(
                new Texture[]{
                        new TextureClip(source, 117,  1,  2, 2),
                        new SubTexture (source, 120,  1, 32, 2),
                        new TextureClip(source, 153,  1,  2, 2),
                        new SubTexture (source, 117,  4,  2, 32),
                        new SubTexture (source, 120,  4, 32, 32),
                        new SubTexture (source, 153,  4,  2, 32),
                        new TextureClip(source, 117, 37,  2, 2),
                        new SubTexture (source, 120, 37, 32, 2),
                        new TextureClip(source, 153, 37,  2, 2),
                }, 2);
    }

    @Override
    public RecipeTemplate createRecipeTemplate(Slot[] slots,
                                               ItemStack craftingType, String backgroundTexture, int backgroundX,
                                               int backgroundY, int backgroundSelectedX, int backgroundSelectedY)
    {
        return createRecipeTemplate(slots, craftingType,
                backgroundTexture, backgroundX, backgroundY,
                backgroundTexture, backgroundSelectedX, backgroundSelectedY);
    }

    @Override
    public RecipeTemplate createRecipeTemplate(Slot[] slots, ItemStack craftingType,
                                               String backgroundTexture, int backgroundX, int backgroundY,
                                               String backgroundSelectedTexture, int backgroundSelectedX, int backgroundSelectedY)
    {
        if(craftingType == null)
        {
            craftingType = workbench;
        }

        if(backgroundTexture.equals("/gui/brewguide.png"))
        {
            backgroundTexture = "craftguide:textures/gui/brewguide.png";
        }
        else if(backgroundTexture.equals("/gui/craftguide.png"))
        {
            backgroundTexture = "craftguide:textures/gui/craftguide.png";
        }
        else if(backgroundTexture.equals("/gui/craftguiderecipe.png"))
        {
            backgroundTexture = "craftguide:textures/gui/craftguiderecipe.png";
        }

        if(backgroundSelectedTexture.equals("/gui/brewguide.png"))
        {
            backgroundSelectedTexture = "craftguide:textures/gui/brewguide.png";
        }
        else if(backgroundSelectedTexture.equals("/gui/craftguide.png"))
        {
            backgroundSelectedTexture = "craftguide:textures/gui/craftguide.png";
        }
        else if(backgroundSelectedTexture.equals("/gui/craftguiderecipe.png"))
        {
            backgroundSelectedTexture = "craftguide:textures/gui/craftguiderecipe.png";
        }

        for(ItemStack stack: recipes.keySet())
        {
            if(ItemStack.areItemStacksEqual(stack, craftingType))
            {
                craftingType = stack;
                break;
            }
        }

        return new DefaultRecipeTemplate(
                slots,
                craftingType,
                new TextureClip(
                        Image.fromJar(backgroundTexture),
                        backgroundX, backgroundY, 79, 58),
                new TextureClip(
                        Image.fromJar(backgroundSelectedTexture),
                        backgroundSelectedX, backgroundSelectedY, 79, 58));
    }

    @Override
    public RecipeTemplate createRecipeTemplate(Slot[] slots, ItemStack craftingType)
    {
        if(craftingType == null)
        {
            craftingType = workbench;
        }

        return new DefaultRecipeTemplate(slots, craftingType, defaultBackground, defaultBackgroundSelected);
    }

    @Override
    public void addRecipe(RecipeTemplate template, Object[] items)
    {
        addRecipe(template.generate(items), template.getCraftingType());
    }

    @Override
    public void addRecipe(CraftGuideRecipe recipe, ItemStack craftingType)
    {
        List<CraftGuideRecipe> recipeList = recipes.get(craftingType);

        if(recipeList == null)
        {
            recipeList = new ArrayList<>();
            recipes.put(craftingType, recipeList);
        }

        recipeList.add(recipe);
    }

    public Map<ItemStack, List<CraftGuideRecipe>> getRecipes()
    {
        return recipes;
    }

    public void clearRecipes()
    {
        recipes.clear();
    }

    @Override
    public void setDefaultTypeVisibility(ItemStack type, boolean visible)
    {
        if(visible)
        {
            disabledTypes.remove(type);
        }
        else if(!disabledTypes.contains(type))
        {
            disabledTypes.add(type);
        }
    }

    @Override
    public Object[] getCraftingRecipe(IRecipe recipe)
    {
        return getCraftingRecipe(recipe, false);
    }

    @Override
    public Object[] getCraftingRecipe(IRecipe recipe, boolean allowSmallGrid) {
        try {
            NonNullList<Ingredient> ingredients = recipe.getIngredients();
            ItemStack output = recipe.getRecipeOutput();

            if (output.isEmpty()) {
                return null;
            }

            if (recipe instanceof ShapedRecipes) {
                ShapedRecipes shaped = (ShapedRecipes) recipe;
                int width = shaped.getWidth();
                int height = shaped.getHeight();

                if (allowSmallGrid && width < 3 && height < 3) {
                    return getSmallShapedRecipe(width, height, ingredients, output);
                } else {
                    return getCraftingShapedRecipe(width, height, ingredients, output);
                }
            } else if (recipe instanceof ShapelessRecipes) {
                return getCraftingShapelessRecipe(ingredients, output);
            } else {
                return ForgeExtensions.getCraftingRecipe(this, recipe, allowSmallGrid);
            }
        } catch (Exception e) {
            CraftGuideLog.log(e, "Exception while trying to parse an ItemStack[10] from an IRecipe:", true);
        }
        return null;
    }

    Object[] getSmallShapedRecipe(int width, int height, NonNullList<Ingredient> ingredients, ItemStack recipeOutput) {
        Object[] output = new Object[5];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                if (index < ingredients.size()) {
                    Ingredient ingredient = ingredients.get(index);
                    output[y * 2 + x] = convertIngredientToObject(ingredient);
                }
            }
        }

        output[4] = recipeOutput;
        return output;
    }

    Object[] getCraftingShapelessRecipe(NonNullList<Ingredient> ingredients, ItemStack recipeOutput) {
        Object[] output = new Object[10];

        for (int i = 0; i < ingredients.size() && i < 9; i++) {
            Ingredient ingredient = ingredients.get(i);
            output[i] = convertIngredientToObject(ingredient);
        }

        output[9] = recipeOutput;
        return output;
    }

    Object[] getCraftingShapedRecipe(int width, int height, NonNullList<Ingredient> ingredients, ItemStack recipeOutput) {
        Object[] output = new Object[10];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                if (index < ingredients.size()) {
                    Ingredient ingredient = ingredients.get(index);
                    output[y * 3 + x] = convertIngredientToObject(ingredient);
                }
            }
        }

        output[9] = recipeOutput;
        return output;
    }

    private Object convertIngredientToObject(Ingredient ingredient) {
        if (ingredient == null || ingredient == Ingredient.EMPTY) {
            return null;
        }

        ItemStack[] matchingStacks = ingredient.getMatchingStacks();
        if (matchingStacks == null || matchingStacks.length == 0) {
            return null;
        }

        List<ItemStack> validStacks = new ArrayList<>();
        for (ItemStack stack : matchingStacks) {
            if (stack != null && !stack.isEmpty()) {
                validStacks.add(stack.copy());
            }
        }

        if (validStacks.isEmpty()) {
            return null;
        }

        if (validStacks.size() == 1) {
            return validStacks.get(0);
        }

        return validStacks.toArray(new ItemStack[0]);
    }

    private Object convertOreIngredientToObject(Ingredient ingredient) {
        if (ingredient == null || ingredient == Ingredient.EMPTY) {
            return null;
        }

        ItemStack[] matchingStacks = ingredient.getMatchingStacks();
        if (matchingStacks == null || matchingStacks.length == 0) {
            return null;
        }

        if (matchingStacks.length > 1) {
            List<ItemStack> validStacks = new ArrayList<>();
            for (ItemStack stack : matchingStacks) {
                if (stack != null && !stack.isEmpty() && stack.getItem() != null) {
                    validStacks.add(stack.copy());
                }
            }

            if (validStacks.isEmpty()) {
                return null;
            }

            return validStacks.toArray(new ItemStack[0]);
        } else if (matchingStacks.length == 1) {
            ItemStack stack = matchingStacks[0];
            if (stack != null && !stack.isEmpty() && stack.getItem() != null) {
                return stack.copy();
            }
        }

        return null;
    }

    @Override
    public RecipeTemplateBuilder buildTemplate(ItemStack type)
    {
        return new RecipeTemplateBuilderImplementation(type);
    }
}