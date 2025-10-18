package com.dhjcomical.craftguide.recipes;

import com.dhjcomical.craftguide.CraftGuideLog;
import com.dhjcomical.craftguide.api.StackInfoSource;
import ic2.api.recipe.IMachineRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipe;
import ic2.api.recipe.Recipes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.Loader;

import java.lang.Iterable;

public class IC2ExperimentalAmplifiers implements StackInfoSource {

    @Override
    public String getInfo(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return null;
        }

        if (!Loader.isModLoaded("ic2")) {
            return null;
        }

        try {
            IMachineRecipeManager<IRecipeInput, Integer, ItemStack> matterAmplifierRecipes = Recipes.matterAmplifier;

            if (matterAmplifierRecipes == null) {
                return null;
            }

            Iterable<? extends MachineRecipe<IRecipeInput, Integer>> recipes = matterAmplifierRecipes.getRecipes();

            for (MachineRecipe<IRecipeInput, Integer> recipe : recipes) {

                IRecipeInput input = recipe.getInput();

                if (input != null && input.matches(itemStack)) {
                    NBTTagCompound metadata = recipe.getMetaData();

                    if (metadata != null && metadata.hasKey("amplification")) {
                        int value = metadata.getInteger("amplification");
                        if (value != 0) {
                            return "\u00a7eMassfab amplifier: " + value;
                        }
                    }

                    return null;
                }
            }

        } catch (Exception e) {
            CraftGuideLog.log(e, "Caught an unexpected exception from IC2 API while checking Massfab amplifier recipes for item: " + itemStack.getTranslationKey(), false);
        }

        return null;
    }
}