package com.dhjcomical.craftguide.mixin.ic2classic;

import ic2.api.recipe.IRecipeInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ic2.core.item.recipe.AdvRecipe.class, remap = false)
public interface IC2ClassicAdvRecipeAccessor {
    @Accessor("length")
    int getLength();

    @Accessor("height")
    int getHeight();

    @Accessor("input")
    IRecipeInput[] getInput();
}