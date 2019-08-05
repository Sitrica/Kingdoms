package com.songoda.kingdoms.database;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.songoda.kingdoms.Kingdoms;

public class YamlDatabase<T> extends Database<T> {
	
	private final FileConfiguration configuration;
	private final Kingdoms instance;
	
	public YamlDatabase() {
		this.instance = Kingdoms.getInstance();
		this.configuration = new YamlConfiguration();
		File file = new File(instance.getDataFolder(), "data.yml");
		try {
			if (!file.exists())
				file.createNewFile();
			configuration.load(file);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public T get(String key, T def) {
		return (T) configuration.get("data." + key, def);
	}

	@Override
	public void put(String key, T value) {
		if (value == null) {
			configuration.set("data." + key, null);
			return;
		}
		configuration.set("data." + key, value);
	}
	
	@Override
	public boolean has(String key) {
		return configuration.isSet("data." + key);
	}

	@Override
	public void clear() {
		configuration.getConfigurationSection("data").set("", null);
		configuration.set("data", null);
	}

	@Override
	public Set<String> getKeys() {
		return configuration.getConfigurationSection("data").getKeys(true);
	}

}
