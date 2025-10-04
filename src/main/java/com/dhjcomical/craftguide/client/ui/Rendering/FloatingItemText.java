package com.dhjcomical.craftguide.client.ui.Rendering;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Mouse;

import com.dhjcomical.craftguide.client.ui.GuiRenderer;
import com.dhjcomical.gui_craftguide.rendering.Renderable;
import com.dhjcomical.gui_craftguide.rendering.RendererBase;


public class FloatingItemText implements Renderable
{
	private List<String> text;
	
	public FloatingItemText(List<String> text)
	{
		this.text = text;
	}
	
	public FloatingItemText(String text)
	{
		this.text = new ArrayList<>(1);
		this.text.add(text);
	}
	
	public void setText(String text)
	{
		this.text = new ArrayList<>(1);
		this.text.add(text);
	}

	public void setText(List<String> text)
	{
		this.text = text;
	}

	//@Override
	public void render(GuiRenderer renderer, int xOffset, int yOffset)
	{
		renderer.drawFloatingText(renderer.guiXFromMouseX(Mouse.getX()) + 12, renderer.guiYFromMouseY(Mouse.getY()) - 13, text);
	}

	@Override
	public void render(RendererBase renderer, int x, int y)
	{
		render((GuiRenderer)renderer, x, y);
	}
}
