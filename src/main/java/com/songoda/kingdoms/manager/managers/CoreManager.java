package com.songoda.kingdoms.manager.managers;

import com.songoda.core.gui.GuiManager;
import com.songoda.kingdoms.manager.Manager;

public class CoreManager extends Manager {

	private final GuiManager guiManager;

	public CoreManager() {
		super(true);
		guiManager = new GuiManager(instance);
	}

	public GuiManager getGuiManager() {
		return guiManager;
	}

	@Override
	public void initalize() {}

	@Override
	public void onDisable() {}

}
