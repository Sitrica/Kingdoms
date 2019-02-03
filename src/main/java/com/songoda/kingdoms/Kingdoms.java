package com.songoda.kingdoms;

import com.songoda.kingdoms.command.CommandHandler;
import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.manager.ManagerHandler;
import com.songoda.kingdoms.objects.ManagerOptional;
import com.songoda.kingdoms.utils.Formatting;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Kingdoms extends JavaPlugin {

	private final Map<String, FileConfiguration> configurations = new HashMap<>();
	private final String packageName = "com.songoda.kingdoms";
	private static String prefix = "[Kingdoms] ";
	public static String user = "%%__USER__%%";
	private CommandHandler commandHandler;
	private ManagerHandler managerHandler;
	private static Kingdoms instance;
	
	@Override
	public void onEnable() {
		instance = this;
		File configFile = new File(getDataFolder(), "config.yml");
		//If newer version was found, update configuration.
		if (!getDescription().getVersion().equals(getConfig().getString("version"))) {
			if (configFile.exists()) configFile.delete();
		}
		//Create all the default files.
		for (String name : Arrays.asList("config", "messages")) {
			File file = new File(getDataFolder(), name + ".yml");
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				saveResource(file.getName(), false);
				debugMessage("created new default file " + file.getName());
			}
			FileConfiguration configuration = new YamlConfiguration();
			try {
				configuration.load(file);
			} catch (IOException | InvalidConfigurationException e) {
				e.printStackTrace();
			}
			configurations.put(name, configuration);
		}
		Bukkit.getPluginManager().registerEvents(new WorldManager(), this);
		commandHandler = new CommandHandler(this);
		managerHandler = new ManagerHandler(instance);
		getLogger().info(Formatting.color("&a============================="));
		getLogger().info(Formatting.color("&7Kingdoms " + getDescription().getVersion() + " by &5Songoda <3&7!"));
		getLogger().info(Formatting.color("&7Kingdoms has been &aEnabled."));
		getLogger().info(Formatting.color("&a============================="));

		
		
		
		

		/*managers = new GameManagement(this);
		guiManagement = new GUIManagement(this);
		//pm.registerEvents(new ScrollerInventoryManager(this), this);
		for(Manager manager : Manager.getModules()){
			if(manager == null){
				continue;
			}
//			if(manager instanceof GeneralAPIManager){
//				return;
//			}
			try {
				pm.registerEvents(manager, this);
				Kingdoms.logInfo(manager.getClass().getSimpleName() + " loaded");
			} catch (Exception e) {
			} catch (Error e){
			}
		}
		WarpPadManager.load();
		while(!postLoadEventQueue.isEmpty()){
			Event e = postLoadEventQueue.poll();
			if(e == null) continue;

			getServer().getPluginManager().callEvent(e);
		}
		*/
	}
	
	public static void consoleMessage(String string) {
		Bukkit.getLogger().info(Formatting.color(prefix + string));
	}
	
	public static void debugMessage(String string) {
		if (instance.getConfig().getBoolean("debug")) {
			consoleMessage("&b" + string);
		}
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
	
	@SuppressWarnings("unchecked")
	public <T extends Manager> ManagerOptional<T> getManager(String name, Class<T> expected) {
		return (ManagerOptional<T>) getManager(name);
	}
	
	public ManagerOptional<Manager> getManager(String name) {
		return managerHandler.getManager(name);
	}
	
	/**
	 * @return The CommandManager allocated to the Kingdoms instance.
	 */
	public CommandHandler getCommandHandler() {
		return commandHandler;
	}
	
	public static Kingdoms getInstance() {
		return instance;
	}
	
	public String getPackageName() {
		return packageName;
	}
	/*
	
	
	
	


	@Override
	public synchronized void onDisable() {
		//2016-05-18
		for (Manager manager : Manager.getModules()){
			if(manager == null){
				continue;
			}
			manager.onDisable();
		}
		ConsoleCommandSender console = Bukkit.getConsoleSender();
		console.sendMessage("&a=============================");
		console.sendMessage("&7Kingdoms " + this.getDescription().getVersion() + " by &5Songoda <3&7!");
		console.sendMessage("&7Action: &cDisabling&7...");
		console.sendMessage("&a=============================");
	}

	public static GameManagement getManagers() {
		return managers;
	}

	public static GUIManagement getGuiManagement() {
		return guiManagement;
	}

	public void reload(){
	    Lang reloading = new Lang(this);
	    lang = reloading;
		reloadConfig();
		new Config(this);
		guiManagement.getNexusGUIManager().init();

	}
	*/
	
}
