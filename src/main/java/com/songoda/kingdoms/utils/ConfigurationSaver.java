package com.songoda.kingdoms.utils;

import java.io.File;
import java.io.IOException;

import com.google.common.io.Files;
import com.songoda.kingdoms.Kingdoms;

public class ConfigurationSaver {

	private final File folder, file, old;

	/**
	 * Save a file and replace it with a new.
	 * 
	 * @param name The name of the old file without .yml
	 * @param instance The instance of Kingdoms.
	 */
	public ConfigurationSaver(String version, String name, Kingdoms instance) {
		folder = new File(instance.getDataFolder(), "old-configurations/");
		if (!folder.exists())
			folder.mkdir();
		old = new File(instance.getDataFolder(), name + ".yml");
		file = new File(folder, version + "-" + name + ".yml");
	}

	public void execute() {
		try {
			Files.move(old, file);
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}

}
