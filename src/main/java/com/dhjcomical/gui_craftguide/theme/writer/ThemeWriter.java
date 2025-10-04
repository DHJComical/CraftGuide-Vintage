package com.dhjcomical.gui_craftguide.theme.writer;

import java.io.OutputStream;

import com.dhjcomical.gui_craftguide.theme.Theme;

public interface ThemeWriter
{
	/**
	 * Writes a Theme to the specified OutputStream.
	 * <br><br>
	 * returns true on success, or false if there was any
	 * sort of failure.
	 * @param theme
	 * @param output
	 * @return
	 */
	public boolean write(Theme theme, OutputStream output);
}
