package com.dhjcomical.craftguide.mixin.ic2exp;

import ic2.api.recipe.IRecipeInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ic2.core.recipe.AdvRecipe.class, remap = false)
public interface IC2AdvRecipeAccessor {
    @Accessor("inputWidth")
    int getInputWidth();

    @Accessor("inputHeight")
    int getInputHeight();

    @Accessor("input")
    IRecipeInput[] getInput();
}