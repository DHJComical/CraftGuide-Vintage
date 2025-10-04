package com.dhjcomical.craftguide;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ItemCraftGuide extends Item
{
	public ItemCraftGuide()
	{
		setTranslationKey("craftguide_item");
		setRegistryName("craftguide_item");
		setCreativeTab(CreativeTabs.MISC);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn)
	{
        ItemStack itemStackIn = playerIn.getHeldItem(handIn);
		CraftGuide.side.openGUI(playerIn);
		return new ActionResult<>(EnumActionResult.SUCCESS, itemStackIn);
	}
}
