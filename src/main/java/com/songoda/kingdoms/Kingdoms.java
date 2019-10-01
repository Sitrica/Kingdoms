package com.songoda.kingdoms;

import com.songoda.core.SongodaCore;
import com.songoda.core.SongodaPlugin;
import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.configuration.Config;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.songoda.kingdoms.api.KingdomsAPI;
import com.songoda.kingdoms.command.ActionCommand;
import com.songoda.kingdoms.command.CommandHandler;
import com.songoda.kingdoms.manager.ExternalManager;
import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.manager.ManagerHandler;
import com.songoda.kingdoms.objects.ManagerOptional;
import com.songoda.kingdoms.placeholders.PlaceholderAPI;
import com.songoda.kingdoms.utils.ConfigurationSaver;
import com.songoda.kingdoms.utils.Formatting;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Kingdoms extends SongodaPlugin {

	private static Kingdoms INSTANCE;

	private final Map<String, FileConfiguration> configurations = new HashMap<>();
	private final String packageName = "com.songoda.kingdoms";
	private static String prefix = "[Kingdoms] ";
	private ManagerHandler managerHandler;
	private CommandHandler commandHandler;
	private ActionCommand actions;

	@Override
	public void onPluginLoad() {
		INSTANCE = this;
	}

	@Override
	public void onPluginEnable() {
		// Register in Songoda Core
		SongodaCore.registerPlugin(this, 65, CompatibleMaterial.GOLDEN_SWORD);

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
				String version = configuration.getString("version", "old");
				if (!version.equalsIgnoreCase(getDescription().getVersion()) && !getConfig().getBoolean("disable-configurations-reset", false)) {
					new ConfigurationSaver(version, name, this).execute();
					file = new File(getDataFolder(), name + ".yml");
					saveResource(file.getName(), false);
					configuration = new YamlConfiguration();
					configuration.load(file);
				}
				configurations.put(name, configuration);
			} catch (IOException | InvalidConfigurationException e) {
				e.printStackTrace();
			}
		}
		managerHandler = new ManagerHandler(INSTANCE);
		managerHandler.start();
		commandHandler = new CommandHandler(this);
		actions = new ActionCommand();
		getCommand("kingdomsaction").setExecutor(actions);
		KingdomsAPI.setInstance(this);
		if	(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null)
			new PlaceholderAPI(this).register();
		consoleMessage("Kingdoms has been enabled");
	}

	@Override
	public void onPluginDisable() {
		managerHandler.getManagers().forEach(manager -> manager.onDisable());
	}

	@Override
	public void onConfigReload() {

	}

	@Override
	public List<Config> getExtraConfig() {
		return null;
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
		if (INSTANCE.getConfig().getBoolean("debug"))
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
		return INSTANCE;
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
