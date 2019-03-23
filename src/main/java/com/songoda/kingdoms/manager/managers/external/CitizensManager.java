package com.songoda.kingdoms.manager.managers.external;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.manager.Manager;

import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

public class CitizensManager extends Manager {

	static {
		registerManager("citizens", new CitizensManager());
	}
	
	private Plugin citizens;
	
	protected CitizensManager() {
		super(false);
		if (!instance.getServer().getPluginManager().isPluginEnabled("Citizens")) {
			citizens = null;
			return;
		}
		citizens = CitizensAPI.getPlugin();
		if (citizens != null)
			Kingdoms.consoleMessage("Hooked into Citizens!");
	}

	public boolean isCitizen(Entity entity) {
		if (entity == null)
			return false;
		if (citizens == null)
			return false;
		if (entity.hasMetadata("NPC"))
			return true;
		if (CitizensAPI.getNPCRegistry().isNPC(entity))
			return true;
		return false;
	}
	
	@Override
	public void onDisable() {}

}
