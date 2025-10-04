package com.dhjcomical.craftguide.recipes;

import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.recipe.Recipes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import com.dhjcomical.craftguide.api.StackInfoSource;

public class IC2ExperimentalAmplifiers implements StackInfoSource {

    @Override
    public String getInfo(ItemStack itemStack) {
        MachineRecipeResult<IRecipeInput, Integer, ItemStack> result = Recipes.matterAmplifier.apply(itemStack, false);

        int value = getValue(result);

        if (value != 0) {
            return "\u00a77Massfab amplifier value: " + value;
        } else {
            return null;
        }
    }

    private static int getValue(MachineRecipeResult<IRecipeInput, Integer, ItemStack> result) {
        if (result != null && result.getRecipe() != null) {
            NBTTagCompound metadata = result.getRecipe().getMetaData();

            if (metadata != null) {
                return metadata.getInteger("amplification");
            }
        }

        return 0;
    }
}