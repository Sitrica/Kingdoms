package com.songoda.kingdoms;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.songoda.kingdoms.command.ActionCommand;
import com.songoda.kingdoms.command.CommandHandler;
import com.songoda.kingdoms.manager.ExternalManager;
import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.manager.ManagerHandler;
import com.songoda.kingdoms.objects.ManagerOptional;
import com.songoda.kingdoms.utils.Formatting;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Kingdoms extends JavaPlugin {

	private final Map<String, FileConfiguration> configurations = new HashMap<>();
	private final String packageName = "com.songoda.kingdoms";
	private static String prefix = "[Kingdoms] ";
	private ManagerHandler managerHandler;
	private CommandHandler commandHandler;
	private static Kingdoms instance;
	private ActionCommand actions;

	@Override
	public void onEnable() {
		instance = this;
		File configFile = new File(getDataFolder(), "config.yml");
		//If newer version was found, update configuration.
		if (!getDescription().getVersion().equals(getConfig().getString("version"))) {
			if (configFile.exists())
				configFile.delete();
		}
		//Create all the default files.
		for (String name : Arrays.asList("config", "messages", "turrets", "structures", "defender-upgrades", "ranks", "arsenal-items", "inventories", "powerups", "misc-upgrades", "map", "sounds")) {
			File file = new File(getDataFolder(), name + ".yml");
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				saveResource(file.getName(), false);
				debugMessage("created new default file " + file.getName());
			}
			FileConfiguration configuration = new YamlConfiguration();
			try {
				configuration.load(file);
				configurations.put(name, configuration);
			} catch (IOException | InvalidConfigurationException e) {
				e.printStackTrace();
			}
		}
		managerHandler = new ManagerHandler(instance);
		managerHandler.start();
		commandHandler = new CommandHandler(this);
		actions = new ActionCommand();
		getCommand("kingdomsaction").setExecutor(actions);
		consoleMessage("Kingdoms has been enabled");
	}

	@Override
	public void onDisable() {
		managerHandler.getManagers().forEach(manager -> manager.onDisable());
	}

	public <T extends ExternalManager> Optional<T> getExternalManager(String name, Class<T> expected) {
		return managerHandler.getExternalManager(name);
	}

	/**
	 * Grab a FileConfiguration allocated to Kingdoms if found.
	 * Call it without it's file extension, just the simple name of the file.
	 * 
	 * @param configuration The name of the configuration to search for.
	 * @return Optional<FileConfiguration> as the file may or may not exist.
	 */
	public Optional<FileConfiguration> getConfiguration(String configuration) {
		return Optional.ofNullable(configurations.get(configuration));
	}

	/**
	 * Grab a Manager by it's name and create it's expected class if not present.
	 * 
	 * @param <T> <T extends Manager>
	 * @param name The name of the Manager.
	 * @param expected The expected Class that extends Manager.
	 * @Deprecated Use {@link #getManager(Class)} instead.
	 * @return The Manager with the defined name.
	 */
	@Deprecated
	public <T extends Manager> T getManager(String name, Class<T> expected) {
		return (T) getManager(expected);
	}

	/**
	 * Grab a Manager by it's class and create it if not present.
	 * 
	 * @param <T> <T extends Manager>
	 * @param expected The expected Class that extends Manager.
	 * @return The Manager that matches the defined class.
	 */
	public <T extends Manager> T getManager(Class<T> expected) {
		return (T) managerHandler.getManager(expected).orElseCreate(expected);
	}

	public ManagerOptional<Manager> getManager(String name) {
		return managerHandler.getManager(name);
	}

	public static void consoleMessage(String string) {
		Bukkit.getConsoleSender().sendMessage(Formatting.color(prefix + string));
	}

	public static void debugMessage(String string) {
		if (instance.getConfig().getBoolean("debug"))
			consoleMessage("&b" + string);
	}

	/**
	 * @return The CommandManager allocated to the Kingdoms instance.
	 */
	public CommandHandler getCommandHandler() {
		return commandHandler;
	}

	public ManagerHandler getManagerHandler() {
		return managerHandler;
	}

	public static Kingdoms getInstance() {
		return instance;
	}

	public List<Manager> getManagers() {
		return managerHandler.getManagers();
	}

	/**
	 * Used to add ActionConsumers to the ActionCommand for ClickEvents.
	 * 
	 * @return ActionCommand used for '/k map' ClickEvents.
	 */
	public ActionCommand getActions() {
		return actions;
	}

	public String getPackageName() {
		return packageName;
	}

}
