package com.dhjcomical.craftguide.client.ui;

import com.dhjcomical.craftguide.RecipeCache;

public interface IRecipeCacheListener
{
	void onChange(RecipeCache cache);
	void onReset(RecipeCache cache);
}
