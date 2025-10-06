package com.dhjcomical.craftguide;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import com.dhjcomical.craftguide.api.ItemFilter;
import com.dhjcomical.craftguide.api.ItemSlot;
import com.dhjcomical.craftguide.api.ItemSlotImplementation;
import com.dhjcomical.craftguide.api.NamedTexture;
import com.dhjcomical.craftguide.api.Renderer;
import com.dhjcomical.craftguide.api.SlotType;
import com.dhjcomical.craftguide.api.Util;

/**
 * It's a rather silly name, but since it's only directly used in one other class...
 */
@SuppressWarnings("deprecation")
public class ItemSlotImplementationImplementation implements ItemSlotImplementation, com.dhjcomical.craftguide.api.slotTypes.ItemSlotImplementation
{
    // ======================= DEBUG SWITCH =======================
    /**
     * Set this to 'true' to enable detailed logging for debugging rendering issues.
     * Set this to 'false' for normal gameplay to avoid spamming the log file.
     */
    public static final boolean DEBUG_MODE = false;
    // ============================================================

    private NamedTexture overlayAny;
    private NamedTexture overlayForge;
    private NamedTexture overlayForgeSingle;
    private NamedTexture background;

    public ItemSlotImplementationImplementation()
    {
        overlayAny = Util.instance.getTexture("ItemStack-Any");
        overlayForge = Util.instance.getTexture("ItemStack-OreDict");
        overlayForgeSingle = Util.instance.getTexture("ItemStack-OreDict-Single");
        background = Util.instance.getTexture("ItemStack-Background");
    }

    @Override
    public List<String> getTooltip(ItemSlot itemSlot, Object data)
    {
        ItemStack stack = item(data);

        if(stack == null)
        {
            if(data instanceof List && ((List<?>) data).isEmpty())
            {
                return emptyOreDictEntryText((List<?>)data);
            }
            else
            {
                return null;
            }
        }
        else
        {
            return CommonUtilities.getExtendedItemStackText(data);
        }
    }

    @Override
    public void draw(ItemSlot itemSlot, Renderer renderer, int recipeX, int recipeY, Object data, boolean isMouseOver)
    {
        if (DEBUG_MODE) CraftGuideLog.log("--- ItemSlot Draw Call Started ---");

        int x = recipeX + itemSlot.x;
        int y = recipeY + itemSlot.y;
        if (DEBUG_MODE) CraftGuideLog.log("  Coordinates: x=" + x + ", y=" + y);

        if (DEBUG_MODE) {
            if (data == null) {
                CraftGuideLog.log("  Data object is NULL.");
            } else {
                CraftGuideLog.log("  Data object type: " + data.getClass().getName());
                if (data instanceof List) {
                    CraftGuideLog.log("  Data is a List with size: " + ((List<?>) data).size());
                } else if (data instanceof ItemStack) {
                    CraftGuideLog.log("  Data is an ItemStack: " + ((ItemStack)data).getDisplayName());
                }
            }
        }

        ItemStack stack = item(data);

        if (itemSlot.drawBackground) {
            if (DEBUG_MODE) CraftGuideLog.log("  Drawing background texture.");
            renderer.renderRect(x - 1, y - 1, 18, 18, background);
        }

        if (stack != null && !stack.isEmpty()) {
            if (DEBUG_MODE) CraftGuideLog.log("  SUCCESS: ItemStack is valid. Item: " + stack.getItem().getRegistryName() + ", Meta: " + stack.getMetadata() + ", Count: " + stack.getCount());

            try {
                if (DEBUG_MODE) CraftGuideLog.log("  Attempting to call renderer.renderItemStack...");
                renderer.renderItemStack(x, y, stack);
                if (DEBUG_MODE) CraftGuideLog.log("  renderer.renderItemStack finished successfully.");
            } catch (Exception e) {
                if (DEBUG_MODE) {
                    CraftGuideLog.log("  !!! CRITICAL: Exception caught during renderer.renderItemStack !!!");
                    CraftGuideLog.log("  Exception Message: " + e.getMessage());
                }
                e.printStackTrace(); // Always print the stack trace for critical errors
            }

            if (isMouseOver) {
                renderer.renderRect(x, y, 16, 16, 0xff, 0xff, 0xff, 0x80);
            }

            if (CommonUtilities.getItemDamage(stack) == CraftGuide.DAMAGE_WILDCARD) {
                if (DEBUG_MODE) CraftGuideLog.log("  Drawing wildcard overlay.");
                renderer.renderRect(x - 1, y - 1, 18, 18, overlayAny);
            }

            if (data instanceof List) {
                if (DEBUG_MODE) CraftGuideLog.log("  Drawing OreDict overlay.");
                if (((List<?>)data).size() > 1) {
                    renderer.renderRect(x - 1, y - 1, 18, 18, overlayForge);
                } else {
                    renderer.renderRect(x - 1, y - 1, 18, 18, overlayForgeSingle);
                }
            }
        } else {
            if (DEBUG_MODE) {
                if (stack == null) {
                    CraftGuideLog.log("  WARNING: ItemStack is NULL. Skipping render.");
                } else { // stack is empty
                    CraftGuideLog.log("  WARNING: ItemStack is EMPTY. Skipping render.");
                }
            }

            if (data instanceof List && ((List<?>)data).isEmpty()) {
                if (DEBUG_MODE) CraftGuideLog.log("  Data is an empty list. Drawing empty OreDict overlay.");
                renderer.renderRect(x - 1, y - 1, 18, 18, overlayForge);
            }
        }
        if (DEBUG_MODE) CraftGuideLog.log("--- ItemSlot Draw Call Finished ---\n");
    }

    private static ItemStack item(Object data)
    {
        if(data == null)
        {
            return null;
        }
        else if(data instanceof ItemStack)
        {
            return (ItemStack)data;
        }
        else if(data instanceof List && ((List<?>)data).size() > 0)
        {
            return item(((List<?>)data).get(0));
        }

        return null;
    }

    @Override
    public boolean matches(ItemSlot itemSlot, ItemFilter search, Object data, SlotType type)
    {
        if(type != itemSlot.slotType && (
                type != SlotType.ANY_SLOT ||
                        itemSlot.slotType == SlotType.DISPLAY_SLOT ||
                        itemSlot.slotType == SlotType.HIDDEN_SLOT))
        {
            return false;
        }

        try
        {
            if(search == null)
            {
                return false;
            }
            else if(data == null || data instanceof ItemStack)
            {
                return search.matches(data);
            }
            else if(data instanceof List)
            {
                for(Object content: (List<?>)data)
                {
                    if(search.matches(content))
                    {
                        return true;
                    }
                }

                return search.matches(data);
            }
        }
        catch (Throwable e)
        {
            // Logging exceptions is generally always a good idea, so no debug switch here.
            CraftGuideLog.log("exception trace: com.dhjcomical.craftguide.ItemSlotImplementationImplementation.matches data " + (data != null? data.getClass() : "null"));
            throw new RuntimeException(e);
        }

        return false;
    }

    @Override
    public boolean isPointInBounds(ItemSlot itemSlot, int x, int y)
    {
        return x >= itemSlot.x
                && x < itemSlot.x + itemSlot.width
                && y >= itemSlot.y
                && y < itemSlot.y + itemSlot.height;
    }

    @Override
    public ItemFilter getClickedFilter(int x, int y, Object object)
    {
        return Util.instance.getCommonFilter(object);
    }

    private List<String> emptyOreDictEntryText(List<?> oreDictionaryList)
    {
        List<String> list = ForgeExtensions.emptyOreDictEntryText(oreDictionaryList);

        if(list == null)
        {
            list = new ArrayList<>(1);
            list.add("Empty item list, not in ore dictionary");
        }

        return list;
    }

    @Override
    public List<String> getTooltip(com.dhjcomical.craftguide.api.slotTypes.ItemSlot itemSlot, Object data)
    {
        ItemStack stack = item(data);

        if(stack == null)
        {
            if(data instanceof List && ((List<?>)data).size() < 1)
            {
                return emptyOreDictEntryText((List<?>)data);
            }
            else
            {
                return null;
            }
        }
        else
        {
            return CommonUtilities.getExtendedItemStackText(data);
        }
    }

    @Override
    public void draw(com.dhjcomical.craftguide.api.slotTypes.ItemSlot itemSlot, Renderer renderer, int recipeX, int recipeY, Object data, boolean isMouseOver)
    {
        int x = recipeX + itemSlot.x;
        int y = recipeY + itemSlot.y;
        ItemStack stack = item(data);

        if(itemSlot.drawBackground)
        {
            renderer.renderRect(x - 1, y - 1, 18, 18, background);
        }

        if(stack != null)
        {
            renderer.renderItemStack(x, y, stack);

            if(isMouseOver)
            {
                renderer.renderRect(x, y, 16, 16, 0xff, 0xff, 0xff, 0x80);
            }

            if(CommonUtilities.getItemDamage(stack) == CraftGuide.DAMAGE_WILDCARD)
            {
                renderer.renderRect(x - 1, y - 1, 18, 18, overlayAny);
            }

            if(data instanceof List)
            {
                if(((List<?>)data).size() > 1)
                {
                    renderer.renderRect(x - 1, y - 1, 18, 18, overlayForge);
                }
                else
                {
                    renderer.renderRect(x - 1, y - 1, 18, 18, overlayForgeSingle);
                }
            }
        }
        else if(data instanceof List && ((List<?>)data).size() < 1)
        {
            renderer.renderRect(x - 1, y - 1, 18, 18, overlayForge);
        }
    }

    @Override
    public boolean matches(com.dhjcomical.craftguide.api.slotTypes.ItemSlot itemSlot, ItemFilter search, Object data, SlotType type)
    {
        if(type != itemSlot.slotType && (
                type != SlotType.ANY_SLOT ||
                        itemSlot.slotType == SlotType.DISPLAY_SLOT ||
                        itemSlot.slotType == SlotType.HIDDEN_SLOT))
        {
            return false;
        }

        try
        {
            if(search == null)
            {
                return false;
            }
            else if(data == null || data instanceof ItemStack)
            {
                return search.matches(data);
            }
            else if(data instanceof List)
            {
                for(Object content: (List<?>)data)
                {
                    if(search.matches(content))
                    {
                        return true;
                    }
                }

                return search.matches(data);
            }
        }
        catch (Throwable e)
        {
            CraftGuideLog.log("exception trace: com.dhjcomical.craftguide.ItemSlotImplementationImplementation.matches data " + (data != null? data.getClass() : "null"));
            throw new RuntimeException(e);
        }

        return false;
    }

    @Override
    public boolean isPointInBounds(com.dhjcomical.craftguide.api.slotTypes.ItemSlot itemSlot, int x, int y)
    {
        return x >= itemSlot.x
                && x < itemSlot.x + itemSlot.width
                && y >= itemSlot.y
                && y < itemSlot.y + itemSlot.height;
    }
}