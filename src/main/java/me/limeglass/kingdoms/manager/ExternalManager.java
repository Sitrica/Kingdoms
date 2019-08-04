package me.limeglass.kingdoms.manager;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;

import me.limeglass.kingdoms.Kingdoms;

public abstract class ExternalManager implements Listener {

	protected final FileConfiguration configuration;
	protected final Kingdoms instance;
	protected final String name;

	protected ExternalManager(String name, boolean listener) {
		this.name = name;
		this.instance = Kingdoms.getInstance();
		this.configuration = instance.getConfig();
		if (listener)
			Bukkit.getPluginManager().registerEvents(this, instance);
	}

	public String getName() {
		return name;
	}

	public abstract boolean isEnabled();

	public abstract void onDisable();

}
