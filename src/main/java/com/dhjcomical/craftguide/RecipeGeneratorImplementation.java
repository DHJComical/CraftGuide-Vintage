package com.dhjcomical.craftguide;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.dhjcomical.craftguide.api.RecipeProvider;
import ic2.api.recipe.IRecipeInput;
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
import ic2.core.recipe.AdvRecipe;
import ic2.core.recipe.AdvShapelessRecipe;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class RecipeGeneratorImplementation implements RecipeGenerator
{
    private final List<RecipeProvider> recipeProviders = new ArrayList<>();
    private static Field shapedRecipes_width, shapedRecipes_height, shapedRecipes_ingredients;
    private static Field shapelessRecipes_ingredients;

    private static Field advRecipe_width, advRecipe_height, advRecipe_input;
    private static Field advShapelessRecipe_input;
    private static Class<?> advRecipeClass;
    private static Class<?> advShapelessRecipeClass;
    static {
        // Vanilla reflection - always runs
        try {
            shapedRecipes_width = ShapedRecipes.class.getDeclaredField("recipeWidth");
            shapedRecipes_width.setAccessible(true);
            shapedRecipes_height = ShapedRecipes.class.getDeclaredField("recipeHeight");
            shapedRecipes_height.setAccessible(true);
            shapedRecipes_ingredients = ShapedRecipes.class.getDeclaredField("recipeItems");
            shapedRecipes_ingredients.setAccessible(true);
            shapelessRecipes_ingredients = ShapelessRecipes.class.getDeclaredField("recipeItems");
            shapelessRecipes_ingredients.setAccessible(true);
        } catch (Exception e) {
            CraftGuideLog.log(e, "Could not find vanilla recipe fields via reflection.", true);
        }

        if (Loader.isModLoaded("ic2")) {
            try {
                advRecipeClass = Class.forName("ic2.core.recipe.AdvRecipe");
                advShapelessRecipeClass = Class.forName("ic2.core.recipe.AdvShapelessRecipe");

                advRecipe_width = advRecipeClass.getDeclaredField("inputWidth");
                advRecipe_width.setAccessible(true);
                advRecipe_height = advRecipeClass.getDeclaredField("inputHeight");
                advRecipe_height.setAccessible(true);
                advRecipe_input = advRecipeClass.getDeclaredField("input");
                advRecipe_input.setAccessible(true);

                advShapelessRecipe_input = advShapelessRecipeClass.getDeclaredField("input");
                advShapelessRecipe_input.setAccessible(true);
            } catch (Exception e) {
                CraftGuideLog.log("Found IC2, but failed to access its recipe classes via reflection. IC2 crafting recipes may not be shown.");
                CraftGuideLog.log(e);
            }
        }
    }

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

    public void addProvider(RecipeProvider provider) {
        this.recipeProviders.add(provider);
    }

    public List<RecipeProvider> getProviders() {
        return this.recipeProviders;
    }

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

    public Map<ItemStack, List<CraftGuideRecipe>> getRecipes() { return recipes; }
    public void clearRecipes() { recipes.clear(); }

    @Override
    public void setDefaultTypeVisibility(ItemStack type, boolean visible) {
        if (visible) {
            disabledTypes.removeIf(stack -> ItemStack.areItemStacksEqual(stack, type));
        } else {
            boolean found = false;
            for(ItemStack disabled: disabledTypes) {
                if(ItemStack.areItemStacksEqual(disabled, type)) {
                    found = true;
                    break;
                }
            }
            if(!found) {
                disabledTypes.add(type);
            }
        }
    }

    @Override
    public Object[] getCraftingRecipe(IRecipe recipe) {
        return getCraftingRecipe(recipe, false);
    }

    @Override
    public Object[] getCraftingRecipe(IRecipe recipe, boolean allowSmallGrid) {
        try {
            if (recipe == null || recipe.getRecipeOutput().isEmpty()) return null;

            ItemStack output = recipe.getRecipeOutput();

            // Check for IC2 AdvRecipe safely
            if (advRecipeClass != null && advRecipeClass.isInstance(recipe)) {
                int width = (int) advRecipe_width.get(recipe);
                int height = (int) advRecipe_height.get(recipe);
                IRecipeInput[] ic2Inputs = (IRecipeInput[]) advRecipe_input.get(recipe);
                NonNullList<Ingredient> ingredients = ingredientsFromIC2Inputs(ic2Inputs);
                if (allowSmallGrid && width <= 2 && height <= 2) return getSmallShapedRecipe(width, height, ingredients, output);
                else return getCraftingShapedRecipe(width, height, ingredients, output);
            }
            // Check for IC2 AdvShapelessRecipe safely
            else if (advShapelessRecipeClass != null && advShapelessRecipeClass.isInstance(recipe)) {
                IRecipeInput[] ic2Inputs = (IRecipeInput[]) advShapelessRecipe_input.get(recipe);
                NonNullList<Ingredient> ingredients = ingredientsFromIC2Inputs(ic2Inputs);
                return getCraftingShapelessRecipe(ingredients, output);
            }
            else if (recipe instanceof ShapedOreRecipe) {
                ShapedOreRecipe shaped = (ShapedOreRecipe) recipe;
                int width = shaped.getRecipeWidth();
                int height = shaped.getRecipeHeight();
                NonNullList<Ingredient> ingredients = shaped.getIngredients();
                if (allowSmallGrid && width <= 2 && height <= 2) return getSmallShapedRecipe(width, height, ingredients, output);
                else return getCraftingShapedRecipe(width, height, ingredients, output);
            }
            else if (recipe instanceof ShapedRecipes) {
                ShapedRecipes shaped = (ShapedRecipes) recipe;
                int width = (int) shapedRecipes_width.get(shaped);
                int height = (int) shapedRecipes_height.get(shaped);
                NonNullList<Ingredient> ingredients = (NonNullList<Ingredient>) shapedRecipes_ingredients.get(shaped);
                if (allowSmallGrid && width <= 2 && height <= 2) return getSmallShapedRecipe(width, height, ingredients, output);
                else return getCraftingShapedRecipe(width, height, ingredients, output);
            }
            else if (recipe instanceof ShapelessOreRecipe || recipe instanceof ShapelessRecipes) {
                NonNullList<Ingredient> ingredients = recipe.getIngredients();
                return getCraftingShapelessRecipe(ingredients, output);
            }
        } catch (Exception e) {
            CraftGuideLog.log(e, "Exception while parsing recipe for " + (recipe != null ? recipe.getRecipeOutput().getDisplayName() : "null"), true);
        }

        return null;
    }

    private NonNullList<Ingredient> ingredientsFromIC2Inputs(IRecipeInput[] ic2Inputs) {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        for (IRecipeInput ic2Input : ic2Inputs) {
            if (ic2Input != null) {
                ingredients.add(Ingredient.fromStacks(ic2Input.getInputs().toArray(new ItemStack[0])));
            } else {
                ingredients.add(Ingredient.EMPTY);
            }
        }
        return ingredients;
    }

    private NonNullList<Ingredient> ingredientsFromObjectArray(Object[] inputArray) {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        for (Object obj : inputArray) {
            if (obj instanceof ItemStack) {
                ingredients.add(Ingredient.fromStacks((ItemStack)obj));
            } else if (obj instanceof String) {
                ingredients.add(new net.minecraftforge.oredict.OreIngredient((String)obj));
            } else if (obj instanceof List) {
                ingredients.add(Ingredient.fromStacks(((List<ItemStack>)obj).toArray(new ItemStack[0])));
            } else {
                ingredients.add(Ingredient.EMPTY);
            }
        }
        return ingredients;
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
        if (ingredient == null || ingredient == Ingredient.EMPTY) { return null; }
        ItemStack[] matchingStacks = ingredient.getMatchingStacks();
        if (matchingStacks == null || matchingStacks.length == 0) { return null; }
        if (matchingStacks.length == 1) {
            return matchingStacks[0].copy();
        } else {
            // Return a list of copies to be safe
            List<ItemStack> stacks = new ArrayList<>();
            for(ItemStack s : matchingStacks) {
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