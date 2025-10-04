package com.dhjcomical.craftguide.client.ui;

public interface IButtonListener
{
	enum Event
	{
		PRESS,
		RELEASE
	}

	void onButtonEvent(GuiButton button, Event eventType);
}
