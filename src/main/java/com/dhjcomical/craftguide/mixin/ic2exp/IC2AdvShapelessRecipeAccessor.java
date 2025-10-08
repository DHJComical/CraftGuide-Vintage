package com.dhjcomical.craftguide.mixin.ic2exp;

import ic2.api.recipe.IRecipeInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ic2.core.recipe.AdvShapelessRecipe.class, remap = false)
public interface IC2AdvShapelessRecipeAccessor {
    @Accessor("input")
    IRecipeInput[] getInput();
}