package com.dhjcomical.craftguide.client.ui;

import com.dhjcomical.craftguide.api.NamedTexture;
import com.dhjcomical.gui_craftguide.rendering.RendererBase;
import com.dhjcomical.gui_craftguide.texture.Texture;

public class NamedTextureObject implements NamedTexture, Texture
{
	private final Texture actualTexture;
	
	public NamedTextureObject(Texture texture)
	{
		actualTexture = texture;
	}
	
	@Override
	public void renderRect(RendererBase renderer, int x, int y, int width, int height, int u, int v)
	{
		actualTexture.renderRect(renderer, x, y, width, height, u, v);
	}
}
