package com.dhjcomical.craftguide.filters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import com.dhjcomical.craftguide.CommonUtilities;
import com.dhjcomical.craftguide.CraftGuideLog;
import com.dhjcomical.craftguide.ForgeExtensions;
import com.dhjcomical.craftguide.api.ItemFilter;
import com.dhjcomical.craftguide.api.NamedTexture;
import com.dhjcomical.craftguide.api.Renderer;
import com.dhjcomical.craftguide.api.Util;

public class StringItemFilter implements ItemFilter
{
    String comparison;
    private NamedTexture textImage = Util.instance.getTexture("TextFilter");

    public StringItemFilter(String string)
    {
        comparison = string.toLowerCase();
    }

    @Override
    public boolean matches(Object item)
    {
        if(item instanceof ItemStack)
        {
            try
            {
                return CommonUtilities.searchExtendedItemStackText(item, comparison);
            }
            catch (Throwable e)
            {
                CraftGuideLog.log("exception trace: uristqwerty.CraftGuide.StringItemFilter.matches ItemStack branch");
                throw new RuntimeException(e);
            }
        }
        else if(item instanceof String)
        {
            try
            {
                return ((String)item).toLowerCase().contains(comparison);
            }
            catch (Throwable e)
            {
                CraftGuideLog.log("exception trace: uristqwerty.CraftGuide.StringItemFilter.matches String branch");
                throw new RuntimeException(e);
            }
        }
        else if(item instanceof List)
        {
            try
            {
                List<?> list = (List<?>)item;
                boolean empty = true;

                for(Object o: list)
                {
                    if(matches(o))
                    {
                        return true;
                    }

                    if(o != null)
                        empty = false;
                }

                if(empty)
                {
                    List<String> lines = ForgeExtensions.emptyOreDictEntryText(list);

                    if(lines != null)
                    {
                        for(String line: lines)
                        {
                            if(line != null && line.toLowerCase().contains(comparison))
                            {
                                return true;
                            }
                        }
                    }
                }


                return false;
            }
            catch (Throwable e)
            {
                CraftGuideLog.log("exception trace: uristqwerty.CraftGuide.StringItemFilter.matches List branch");
                throw new RuntimeException(e);
            }
        }
        else
        {
            return false;
        }
    }

    @Override
    public void draw(Renderer renderer, int x, int y)
    {
        renderer.renderRect(x, y, 16, 16, textImage);
    }

    @Override
    public List<String> getTooltip()
    {
        List<String> text = new ArrayList<>(1);
        text.add("\u00a77Text search: '" + comparison + "'");
        return text;
    }

    @Override
    public List<ItemStack> getRepresentativeItems()
    {
        List<ItemStack> items = new ArrayList<>();

        for(Item item : Item.REGISTRY)
        {
            if(item == null) continue;

            try
            {
                ItemStack stack = new ItemStack(item);
                if(!stack.isEmpty() && matches(stack))
                {
                    items.add(stack);
                }
            }
            catch(Exception ignored)
            {

            }
        }

        return items;
    }
}