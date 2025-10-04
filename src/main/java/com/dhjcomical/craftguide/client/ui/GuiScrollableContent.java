package com.dhjcomical.craftguide.client.ui;

import com.dhjcomical.craftguide.client.ui.GuiScrollBar.ScrollBarAlignmentCallback;
import com.dhjcomical.gui_craftguide.components.GuiElement;

public abstract class GuiScrollableContent extends GuiElement implements ScrollBarAlignmentCallback
{
	protected GuiScrollBar scrollBar;

	protected int lastMouseX, lastMouseY;
	protected float lastScroll;

	public GuiScrollableContent(int x, int y, int width, int height, GuiScrollBar scrollBar)
	{
		super(x, y, width, height);

		this.scrollBar = scrollBar;
	}

	@Override
	public float alignScrollBar(GuiScrollBar guiScrollBar, float oldValue, float newValue)
	{
		return newValue;
	}

	@Override
	public void draw()
	{
		if(lastScroll != scrollBar.getValue())
		{
			lastScroll = scrollBar.getValue();
			mouseMoved(lastMouseX, lastMouseY);
		}

		super.draw();
	}

	@Override
	public void mouseMoved(int x, int y)
	{
		lastMouseX = x;
		lastMouseY = y;
		lastScroll = scrollBar.getValue();

		super.mouseMoved(x, y);
	}
}
