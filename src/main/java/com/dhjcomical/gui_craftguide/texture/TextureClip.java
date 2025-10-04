package com.dhjcomical.gui_craftguide.texture;

import com.dhjcomical.craftguide.CraftGuideLog;
import com.dhjcomical.gui_craftguide.Rect;
import com.dhjcomical.gui_craftguide.editor.TextureMeta;
import com.dhjcomical.gui_craftguide.editor.TextureMeta.TextureParameter;
import com.dhjcomical.gui_craftguide.rendering.RendererBase;

/**
 * Represents a subsection of a larger texture, shifted so that
 * the sections's top left corner is at (0, 0). When drawn, only
 * draws the portion, if any, of the drawn area that overlaps with
 * the subsection.
 */
@TextureMeta(name = "clip")
public class TextureClip implements Texture
{
	@TextureParameter
	public Texture source;

	@TextureParameter
	public Rect rect;

	public TextureClip(Texture source, int u, int v, int width, int height)
	{
		this.source = source;
		rect = new Rect(u, v, width, height);
	}

	public TextureClip(Texture source, Rect rect)
	{
		this.source = source;
		this.rect = rect;
	}

	public TextureClip()
	{
	}

    @Override
    public void renderRect(RendererBase renderer, int x, int y, int width, int height, int u, int v) {
        Texture realSource = this.source;

        while (realSource != null && !(realSource instanceof BasicTexture)) {
            if (realSource instanceof DynamicTexture) {
                realSource = ((DynamicTexture) realSource).getTexture();
            } else if (realSource instanceof TextureClip) {
                realSource = ((TextureClip) realSource).source;
            } else {
                CraftGuideLog.log("Rendering error: Found unknown texture type in chain: " + realSource.getClass().getName());
                return;
            }
        }

        if (realSource instanceof BasicTexture) {
            if (u < 0) { width += u; u = 0; }
            if (u + width > rect.width) { width = rect.width - u; }
            if (v < 0) { height += v; v = 0; }
            if (v + height > rect.height) { height = rect.height - v; }

            if (width > 0 && height > 0) {
                realSource.renderRect(renderer, x, y, width, height, u + rect.x, v + rect.y);
            }
        } else {
             CraftGuideLog.log("Rendering error: ClipTexture could not resolve its source chain to a BasicTexture.");
        }
    }

	@Override
	public boolean equals(Object obj)
	{
		if(!(obj instanceof TextureClip))
			return false;

		TextureClip other = (TextureClip)obj;

		return (this.source == null || other.source == null?
				this.source == other.source :
				this.source.equals(other.source)) && this.rect.equals(other.rect);
	}

	@Override
	public int hashCode()
	{
		// TODO Auto-generated method stub
		return super.hashCode();
	}
}
