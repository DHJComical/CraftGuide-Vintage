package com.dhjcomical.craftguide;

import com.dhjcomical.craftguide.api.*;
import com.dhjcomical.craftguide.mixin.ShapedRecipesAccessor;
import com.dhjcomical.craftguide.mixin.ShapelessRecipesAccessor;
import com.dhjcomical.craftguide.mixin.ic2exp.IC2AdvRecipeAccessor;
import com.dhjcomical.craftguide.mixin.ic2exp.IC2AdvShapelessRecipeAccessor;
import com.dhjcomical.craftguide.mixin.ic2classic.IC2ClassicAdvRecipeAccessor;
import com.dhjcomical.craftguide.template_builder.RecipeTemplateBuilderImplementation;
import com.dhjcomical.gui_craftguide.minecraft.Image;
import com.dhjcomical.gui_craftguide.texture.*;
import ic2.api.recipe.IRecipeInput;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RecipeGeneratorImplementation implements RecipeGenerator {


    private final List<RecipeProvider> recipeProviders = new ArrayList<>();
    private Map<ItemStack, List<CraftGuideRecipe>> recipes = new HashMap<>();
    public List<ItemStack> disabledTypes = new LinkedList<>();
    public Texture defaultBackground = new BlankTexture();
    public Texture defaultBackgroundSelected;
    public static ItemStack workbench = new ItemStack(Blocks.CRAFTING_TABLE);
    public static final RecipeGeneratorImplementation instance = new RecipeGeneratorImplementation();

    private RecipeGeneratorImplementation() {
        Texture source = DynamicTexture.instance("base_image");
        defaultBackgroundSelected = new BorderedTexture(new Texture[]{
                new TextureClip(source, 117, 1, 2, 2), new SubTexture(source, 120, 1, 32, 2), new TextureClip(source, 153, 1, 2, 2),
                new SubTexture(source, 117, 4, 2, 32), new SubTexture(source, 120, 4, 32, 32), new SubTexture(source, 153, 4, 2, 32),
                new TextureClip(source, 117, 37, 2, 2), new SubTexture(source, 120, 37, 32, 2), new TextureClip(source, 153, 37, 2, 2),
        }, 2);
    }

    public void addProvider(RecipeProvider provider) { this.recipeProviders.add(provider); }
    public List<RecipeProvider> getProviders() { return this.recipeProviders; }

    public void generateRecipesByProviders() {
        for (RecipeProvider provider : this.recipeProviders) {
            try {
                provider.generateRecipes(this);
            } catch (Throwable e) {
                CraftGuideLog.log("A recipe provider crashed during recipe generation: " + provider.getClass().getName());
                CraftGuideLog.log(e);
            }
        }
    }

    @Override
    public Object[] getCraftingRecipe(IRecipe recipe, boolean allowSmallGrid) {
        try {
            if (recipe == null || recipe.getRecipeOutput().isEmpty()) return null;
            ItemStack output = recipe.getRecipeOutput();
            String recipeClassName = recipe.getClass().getName();

            if (Loader.isModLoaded("ic2")) {
                if (recipeClassName.equals("ic2.core.recipe.AdvRecipe")) {
                    IC2AdvRecipeAccessor shaped = (IC2AdvRecipeAccessor) recipe;
                    int width = shaped.getInputWidth();
                    int height = shaped.getInputHeight();
                    NonNullList<Ingredient> ingredients = ingredientsFromIC2Inputs(shaped.getInput());
                    if (allowSmallGrid && width <= 2 && height <= 2) return getSmallShapedRecipe(width, height, ingredients, output);
                    return getCraftingShapedRecipe(width, height, ingredients, output);
                }
                if (recipeClassName.equals("ic2.core.recipe.AdvShapelessRecipe")) {
                    IC2AdvShapelessRecipeAccessor shapeless = (IC2AdvShapelessRecipeAccessor) recipe;
                    NonNullList<Ingredient> ingredients = ingredientsFromIC2Inputs(shapeless.getInput());
                    return getCraftingShapelessRecipe(ingredients, output);
                }
                if (recipeClassName.equals("ic2.core.item.recipe.AdvRecipe")) {
                    IC2ClassicAdvRecipeAccessor classic = (IC2ClassicAdvRecipeAccessor) recipe;
                    NonNullList<Ingredient> ingredients = ingredientsFromIC2Inputs(classic.getInput());
                    int width = classic.getLength();
                    int height = classic.getHeight();
                    if (width > 0 && height > 0) { // Shaped
                        if (allowSmallGrid && width <= 2 && height <= 2) return getSmallShapedRecipe(width, height, ingredients, output);
                        return getCraftingShapedRecipe(width, height, ingredients, output);
                    } else { // Shapeless
                        return getCraftingShapelessRecipe(ingredients, output);
                    }
                }
            }

            if (recipe instanceof ShapedOreRecipe) {
                ShapedOreRecipe shaped = (ShapedOreRecipe) recipe;
                int width = shaped.getRecipeWidth();
                int height = shaped.getRecipeHeight();
                NonNullList<Ingredient> ingredients = shaped.getIngredients();
                if (allowSmallGrid && width <= 2 && height <= 2) return getSmallShapedRecipe(width, height, ingredients, output);
                return getCraftingShapedRecipe(width, height, ingredients, output);
            }
            if (recipe instanceof ShapelessOreRecipe) {
                NonNullList<Ingredient> ingredients = recipe.getIngredients();
                return getCraftingShapelessRecipe(ingredients, output);
            }
            if (recipe instanceof ShapedRecipes) {
                ShapedRecipesAccessor shaped = (ShapedRecipesAccessor) recipe;
                int width = shaped.getRecipeWidth();
                int height = shaped.getRecipeHeight();
                NonNullList<Ingredient> ingredients = shaped.getRecipeItems();
                if (allowSmallGrid && width <= 2 && height <= 2) return getSmallShapedRecipe(width, height, ingredients, output);
                return getCraftingShapedRecipe(width, height, ingredients, output);
            }
            if (recipe instanceof ShapelessRecipes) {
                ShapelessRecipesAccessor shapeless = (ShapelessRecipesAccessor) recipe;
                NonNullList<Ingredient> ingredients = shapeless.getRecipeItems();
                return getCraftingShapelessRecipe(ingredients, output);
            }

        } catch (Exception e) {
            CraftGuideLog.log(e, "Exception while parsing recipe for " + (recipe != null ? recipe.getRecipeOutput().getDisplayName() : "null"), true);
        }
        return null;
    }

    private NonNullList<Ingredient> ingredientsFromIC2Inputs(IRecipeInput[] ic2Inputs) {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        if (ic2Inputs == null) return ingredients;
        for (IRecipeInput ic2Input : ic2Inputs) {
            if (ic2Input != null && !ic2Input.getInputs().isEmpty()) {
                ingredients.add(Ingredient.fromStacks(ic2Input.getInputs().toArray(new ItemStack[0])));
            } else {
                ingredients.add(Ingredient.EMPTY);
            }
        }
        return ingredients;
    }


    @Override
    public RecipeTemplate createRecipeTemplate(Slot[] slots, ItemStack craftingType, String backgroundTexture, int backgroundX, int backgroundY, int backgroundSelectedX, int backgroundSelectedY) {
        return createRecipeTemplate(slots, craftingType, backgroundTexture, backgroundX, backgroundY, backgroundTexture, backgroundSelectedX, backgroundSelectedY);
    }

    @Override
    public RecipeTemplate createRecipeTemplate(Slot[] slots, ItemStack craftingType, String backgroundTexture, int backgroundX, int backgroundY, String backgroundSelectedTexture, int backgroundSelectedX, int backgroundSelectedY) {
        if (craftingType == null) { craftingType = workbench; }
        if (backgroundTexture.equals("/gui/brewguide.png")) { backgroundTexture = "craftguide:textures/gui/brewguide.png"; }
        else if (backgroundTexture.equals("/gui/craftguide.png")) { backgroundTexture = "craftguide:textures/gui/craftguide.png"; }
        else if (backgroundTexture.equals("/gui/craftguiderecipe.png")) { backgroundTexture = "craftguide:textures/gui/craftguiderecipe.png"; }
        if (backgroundSelectedTexture.equals("/gui/brewguide.png")) { backgroundSelectedTexture = "craftguide:textures/gui/brewguide.png"; }
        else if (backgroundSelectedTexture.equals("/gui/craftguide.png")) { backgroundSelectedTexture = "craftguide:textures/gui/craftguide.png"; }
        else if (backgroundSelectedTexture.equals("/gui/craftguiderecipe.png")) { backgroundSelectedTexture = "craftguide:textures/gui/craftguiderecipe.png"; }
        for (ItemStack stack : recipes.keySet()) { if (ItemStack.areItemStacksEqual(stack, craftingType)) { craftingType = stack; break; } }
        return new DefaultRecipeTemplate(slots, craftingType, new TextureClip(Image.fromJar(backgroundTexture), backgroundX, backgroundY, 79, 58), new TextureClip(Image.fromJar(backgroundSelectedTexture), backgroundSelectedX, backgroundSelectedY, 79, 58));
    }

    @Override
    public RecipeTemplate createRecipeTemplate(Slot[] slots, ItemStack craftingType) {
        if (craftingType == null) { craftingType = workbench; }
        return new DefaultRecipeTemplate(slots, craftingType, defaultBackground, defaultBackgroundSelected);
    }

    @Override
    public void addRecipe(RecipeTemplate template, Object[] items) {
        addRecipe(template.generate(items), template.getCraftingType());
    }

    @Override
    public void addRecipe(CraftGuideRecipe recipe, ItemStack craftingType) {
        recipes.computeIfAbsent(craftingType, k -> new ArrayList<>()).add(recipe);
    }

    public Map<ItemStack, List<CraftGuideRecipe>> getRecipes() {
        return recipes;
    }

    public void clearRecipes() {
        recipes.clear();
    }

    @Override
    public void setDefaultTypeVisibility(ItemStack type, boolean visible) {
        if (visible) {
            disabledTypes.removeIf(stack -> ItemStack.areItemStacksEqual(stack, type));
        } else {
            boolean found = false;
            for (ItemStack disabled : disabledTypes) {
                if (ItemStack.areItemStacksEqual(disabled, type)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                disabledTypes.add(type);
            }
        }
    }

    @Override
    public Object[] getCraftingRecipe(IRecipe recipe) {
        return getCraftingRecipe(recipe, false);
    }

    Object[] getSmallShapedRecipe(int width, int height, NonNullList<Ingredient> ingredients, ItemStack recipeOutput) {
        Object[] output = new Object[5];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                if (index < ingredients.size()) {
                    output[y * 2 + x] = convertIngredientToObject(ingredients.get(index));
                }
            }
        }
        output[4] = recipeOutput;
        return output;
    }

    Object[] getCraftingShapelessRecipe(NonNullList<Ingredient> ingredients, ItemStack recipeOutput) {
        Object[] output = new Object[10];
        for (int i = 0; i < ingredients.size() && i < 9; i++) {
            output[i] = convertIngredientToObject(ingredients.get(i));
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
                    output[y * 3 + x] = convertIngredientToObject(ingredients.get(index));
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
        if (matchingStacks.length == 1) {
            return matchingStacks[0].copy();
        } else {
            List<ItemStack> stacks = new ArrayList<>();
            for (ItemStack s : matchingStacks) {
                stacks.add(s.copy());
            }
            return stacks;
        }
    }

    @Override
    public RecipeTemplateBuilder buildTemplate(ItemStack type) {
        return new RecipeTemplateBuilderImplementation(type);
    }
}