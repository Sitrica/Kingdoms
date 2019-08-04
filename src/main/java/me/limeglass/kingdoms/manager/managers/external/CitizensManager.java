package me.limeglass.kingdoms.manager.managers.external;

import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import me.limeglass.kingdoms.Kingdoms;
import me.limeglass.kingdoms.manager.ExternalManager;

public class CitizensManager extends ExternalManager {

	private Plugin citizens;

	public CitizensManager() {
		super("citizens", false);
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
	public boolean isEnabled() {
		return citizens != null;
	}

	@Override
	public void onDisable() {}

}
