package com.dhjcomical.craftguide.recipes;

import com.dhjcomical.craftguide.api.ConstructedRecipeTemplate;
import com.dhjcomical.craftguide.api.RecipeGenerator;
import com.dhjcomical.craftguide.api.RecipeProvider;
import com.dhjcomical.craftguide.itemtype.ItemType;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.HashSet;
import java.util.Set;

public class BrewingRecipes implements RecipeProvider {

    @Override
    public void generateRecipes(RecipeGenerator generator) {
        Set<ItemType> allIngredients = new HashSet<>();
        Set<ItemType> allKnownPotions = new HashSet<>();

        // 1. Gather all valid ingredients and initial potion types
        for (Item item : ForgeRegistries.ITEMS.getValuesCollection()) {
            NonNullList<ItemStack> subItems = NonNullList.create();
            item.getSubItems(CreativeTabs.SEARCH, subItems);

            for (ItemStack stack : subItems) {
                if (stack.isEmpty()) continue;

                if (BrewingRecipeRegistry.isValidIngredient(stack)) {
                    ItemType type = ItemType.getInstance(stack);
                    if (type != null) allIngredients.add(type);
                }

                if (BrewingRecipeRegistry.isValidInput(stack)) {
                    ItemType type = ItemType.getInstance(stack);
                    if (type != null) allKnownPotions.add(type);
                }
            }
        }

        ItemStack brewingStand = new ItemStack(Items.BREWING_STAND);
        ConstructedRecipeTemplate template = generator.buildTemplate(brewingStand)
                .item().item().nextColumn(1)
                .machineItem().nextColumn(1)
                .outputItem().finishTemplate();

        // 2. Iteratively discover new potions
        Set<ItemType> newlyDiscoveredPotions;
        Set<ItemType> potionsToTestInNextLoop = new HashSet<>(allKnownPotions);

        for (int i = 0; i < 10 && !potionsToTestInNextLoop.isEmpty(); i++) {
            newlyDiscoveredPotions = new HashSet<>();

            for (IBrewingRecipe recipe : BrewingRecipeRegistry.getRecipes()) {
                for (ItemType ingredientType : allIngredients) {
                    ItemStack ingredientStack = ingredientType.getDisplayStack();
                    if (!recipe.isIngredient(ingredientStack)) continue;

                    for (ItemType inputPotionType : potionsToTestInNextLoop) {
                        ItemStack inputPotionStack = inputPotionType.getDisplayStack();
                        if (!recipe.isInput(inputPotionStack)) continue;

                        ItemStack resultStack = recipe.getOutput(inputPotionStack, ingredientStack);

                        if (resultStack != null && !resultStack.isEmpty()) {
                            ItemType resultType = ItemType.getInstance(resultStack);
                            if (resultType == null) continue;

                            template.buildRecipe()
                                    .item(ingredientStack)
                                    .item(inputPotionStack)
                                    .item(brewingStand)
                                    .item(resultStack)
                                    .addRecipe(generator);

                            if (allKnownPotions.add(resultType)) {
                                newlyDiscoveredPotions.add(resultType);
                            }
                        }
                    }
                }
            }
            potionsToTestInNextLoop = newlyDiscoveredPotions;
        }

        generator.setDefaultTypeVisibility(brewingStand, false);
    }
}