package com.dhjcomical.craftguide;

import net.minecraft.item.ItemStack;
import com.dhjcomical.craftguide.api.CraftGuideRecipe;
import com.dhjcomical.craftguide.api.RecipeTemplate;
import com.dhjcomical.craftguide.api.Slot;
import com.dhjcomical.gui_craftguide.rendering.Renderable;
import com.dhjcomical.gui_craftguide.rendering.TexturedRect;
import com.dhjcomical.gui_craftguide.texture.Texture;

public class DefaultRecipeTemplate implements RecipeTemplate
{
	private Slot[] slots;
	private Texture backgroundTexture, backgroundSelectedTexture;
	private Renderable background, backgroundSelected;
	private int width = 79, height = 58;

	private ItemStack craftingType;

	public DefaultRecipeTemplate(Slot[] slots, ItemStack craftingType, Texture background, Texture backgroundSelected)
	{
		this.slots = slots;
		this.backgroundTexture = background;
		this.backgroundSelectedTexture = backgroundSelected;
		this.background = new TexturedRect(0, 0, width, height, background, 0, 0);
		this.backgroundSelected = new TexturedRect(0, 0, width, height, backgroundSelected, 0, 0);
		this.craftingType = craftingType;
	}

	@Override
	public CraftGuideRecipe generate(Object[] items)
	{
		return new Recipe(slots, items, background, backgroundSelected).setSize(width, height);
	}

	public Recipe generateWithSize(Object[] items, int width, int height)
	{
		TexturedRect background = new TexturedRect(0, 0, width, height, backgroundTexture, 0, 0);
		TexturedRect backgroundSelected = new TexturedRect(0, 0, width, height, backgroundSelectedTexture, 0, 0);
		return new Recipe(slots, items, background, backgroundSelected).setSize(width, height);
	}

	@Override
	public RecipeTemplate setSize(int width, int height)
	{
		this.width = width;
		this.height = height;

		background = new TexturedRect(0, 0, width, height, backgroundTexture, 0, 0);
		backgroundSelected = new TexturedRect(0, 0, width, height, backgroundSelectedTexture, 0, 0);

		return this;
	}

	@Override
	public ItemStack getCraftingType()
	{
		return craftingType;
	}

	public int width()
	{
		return width;
	}

	public int height()
	{
		return height;
	}
}
