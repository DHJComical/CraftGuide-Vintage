package com.dhjcomical.craftguide.recipes;

import ic2.api.info.Info;
import net.minecraft.item.ItemStack;
import com.dhjcomical.craftguide.api.StackInfoSource;

public class IC2GeneratorFuel implements StackInfoSource {

    public IC2GeneratorFuel() {
    }

    @Override
    public String getInfo(ItemStack itemStack) {
        int fuel = Info.itemInfo.getFuelValue(itemStack, false);
        // --------------------------------------------------------------------------

        if (fuel > 0) {
            return "\u00a77" + (fuel / 4) + " EU in an IndustrialCraft generator";
        }

        return null;
    }

}