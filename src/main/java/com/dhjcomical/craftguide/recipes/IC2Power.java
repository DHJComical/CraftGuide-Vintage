package com.dhjcomical.craftguide.recipes;

import ic2.api.info.Info;
import ic2.api.item.IElectricItem;
import net.minecraft.item.ItemStack;
import com.dhjcomical.craftguide.api.StackInfoSource;

public class IC2Power implements StackInfoSource
{
	@Override
	public String getInfo(ItemStack itemStack)
	{
		if(itemStack.getItem() instanceof IElectricItem)
		{
			return "\u00a77Can store " + ((IElectricItem)itemStack.getItem()).getMaxCharge(itemStack) + " EU";
		}
		else
		{
			double power = Info.itemInfo.getEnergyValue(itemStack);

			if(power > 0)
			{
				return "\u00a77Powers IC2 machines for " + power + " EU";
			}
		}

		return null;
	}
}
