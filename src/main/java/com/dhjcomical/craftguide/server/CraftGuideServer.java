package com.dhjcomical.craftguide.server;

import net.minecraft.entity.player.EntityPlayer;
import com.dhjcomical.craftguide.CraftGuideSide;
import com.dhjcomical.craftguide.api.Util;

public class CraftGuideServer implements CraftGuideSide
{
	@Override
	public void initKeybind()
	{
	}

	@Override
	public void checkKeybind()
	{
	}

	@Override
	public void preInit()
	{
		Util.instance = new UtilImplementationServer();
	}

	@Override
	public void reloadRecipes()
	{
	}

	@Override
	public void openGUI(EntityPlayer player)
	{
	}

	@Override
	public void initNetworkChannels()
	{
	}

	@Override
	public void stopTessellating()
	{
	}
}
