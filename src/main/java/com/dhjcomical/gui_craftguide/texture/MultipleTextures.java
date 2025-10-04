package com.dhjcomical.gui_craftguide.texture;

import com.dhjcomical.gui_craftguide.editor.TextureMeta;
import com.dhjcomical.gui_craftguide.editor.TextureMeta.TextureParameter;
import com.dhjcomical.gui_craftguide.rendering.RendererBase;

@TextureMeta(name = "multipletextures")
public class MultipleTextures implements Texture
{
	@TextureParameter
	public Texture[] textures;

	public MultipleTextures()
	{
	}

	public MultipleTextures(Texture[] textures)
	{
		this.textures = textures;
	}

	@Override
	public void renderRect(RendererBase renderer, int x, int y, int width, int height, int u, int v)
	{
		for(Texture texture: textures)
		{
			texture.renderRect(renderer, x, y, width, height, u, v);
		}
	}
}
