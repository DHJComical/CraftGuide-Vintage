package com.dhjcomical.craftguide.filters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.item.ItemStack;
import com.dhjcomical.craftguide.api.ItemFilter;
import com.dhjcomical.craftguide.api.NamedTexture;
import com.dhjcomical.craftguide.api.Renderer;
import com.dhjcomical.craftguide.api.Util;

public class EmptyOreDictionaryFilter implements ItemFilter
{
    private static final NamedTexture overlayForge = Util.instance.getTexture("ItemStack-OreDict");

    private final List<String> tooltip;
    private final List<ItemStack> list;

    public EmptyOreDictionaryFilter(List<ItemStack> stack, String oreDictionaryName)
    {
        list = stack;

        tooltip = new ArrayList<>(1);
        tooltip.add("\u00a77Empty OreDictionary list: '" + oreDictionaryName + "'");
    }

    @Override
    public boolean matches(Object item)
    {
        return item == list;
    }

    @Override
    public void draw(Renderer renderer, int x, int y)
    {
        renderer.renderRect(x - 1, y - 1, 18, 18, overlayForge);
    }

    @Override
    public List<String> getTooltip()
    {
        return tooltip;
    }

    @Override
    public List<ItemStack> getRepresentativeItems()
    {
        return Collections.emptyList();
    }
}