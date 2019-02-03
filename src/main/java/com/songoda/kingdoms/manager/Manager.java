package com.songoda.kingdoms.manager;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import com.songoda.kingdoms.Kingdoms;

public abstract class Manager implements Listener {
	
	protected Manager(boolean listener) {
		if (listener)
			Bukkit.getPluginManager().registerEvents(this, Kingdoms.getInstance());
	}
	
	protected static void registerManager(String name, Manager manager) {
		ManagerHandler.registerManager(name, manager);
	}
	
	public abstract void onDisable();

}
