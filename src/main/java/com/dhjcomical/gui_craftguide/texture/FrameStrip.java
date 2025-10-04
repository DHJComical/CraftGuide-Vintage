package com.dhjcomical.gui_craftguide.texture;

import com.dhjcomical.gui_craftguide.editor.TextureMeta.TextureParameter;
import com.dhjcomical.gui_craftguide.editor.TextureMeta.Unit;
import com.dhjcomical.gui_craftguide.editor.TextureMeta.WithUnits;

public class FrameStrip
{
	@TextureParameter
	public int width;

	@TextureParameter
	public int height;

	@TextureParameter
	public int x;

	@TextureParameter
	public int y;

	@TextureParameter
	public int xstep;

	@TextureParameter
	public int ystep;

	@TextureParameter
	public int framecount;

	@TextureParameter
	@WithUnits({
		@Unit(multiplier = 1, names = {"second", "seconds", "s"}),
		@Unit(multiplier = 1/1000.0, names = {"millisecond", "milliseconds", "ms"}),
		@Unit(multiplier = 1/20.0, names = {"tick", "ticks", "t"}),
	})
	public double frameduration;
}
