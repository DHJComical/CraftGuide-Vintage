package com.dhjcomical.gui_craftguide.theme.reader;

public interface ValueTemplate extends ElementHandler
{
	public Class<?> valueType();
	public Object getValue();
}
