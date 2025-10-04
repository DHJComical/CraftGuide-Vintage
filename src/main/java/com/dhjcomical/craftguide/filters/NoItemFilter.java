package com.dhjcomical.craftguide.filters;

import java.util.Collections;
import java.util.List;

import com.dhjcomical.craftguide.api.ItemFilter;
import com.dhjcomical.craftguide.api.Renderer;
import net.minecraft.item.ItemStack;

public class NoItemFilter implements ItemFilter
{
    @Override
    public boolean matches(Object item)
    {
        return false;
    }

    @Override
    public void draw(Renderer renderer, int x, int y)
    {
    }

    @Override
    public List<String> getTooltip()
    {
        return null;
    }

    @Override
    public List<ItemStack> getRepresentativeItems()
    {
        return Collections.emptyList();
    }
}