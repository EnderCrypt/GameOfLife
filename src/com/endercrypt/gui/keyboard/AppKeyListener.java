package com.endercrypt.gui.keyboard;

@FunctionalInterface
public interface AppKeyListener
{
	void keyTriggered(int keycode, Keyboard.BindType bindType);
}
