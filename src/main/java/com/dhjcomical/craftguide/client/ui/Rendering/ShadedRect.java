package com.dhjcomical.craftguide.client.ui.Rendering;

import com.dhjcomical.craftguide.client.ui.GuiRenderer;
import com.dhjcomical.gui_craftguide.rendering.Renderable;
import com.dhjcomical.gui_craftguide.rendering.RendererBase;

public class ShadedRect implements Renderable
{
	private int x, y, width, height;
	private int color, alpha;

	public ShadedRect(int x, int y, int width, int height, int colour)
	{
		this(x, y, width, height, colour, 0xff);
	}
	
	public ShadedRect(int x, int y, int width, int height, int color, int alpha)
	{
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.color = color;
		this.alpha = alpha;
	}
	
	//@Override
	public void render(GuiRenderer renderer, int xOffset, int yOffset)
	{
		renderer.setColor(color, alpha);
		renderer.drawRect(x + xOffset, y + yOffset, width, height);
		renderer.setColor(0xffffff, 0xff);
	}
	
	@Override
	public void render(RendererBase renderer, int x, int y)
	{
		render((GuiRenderer)renderer, x, y);
	}
}
