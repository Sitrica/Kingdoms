package com.songoda.kingdoms.manager;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;

import com.google.common.collect.Sets;
import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.database.Database;
import com.songoda.kingdoms.database.DatabaseTransferTask;
import com.songoda.kingdoms.database.MySQLDatabase;
import com.songoda.kingdoms.database.SQLiteDatabase;
import com.songoda.kingdoms.database.YamlDatabase;
import com.songoda.kingdoms.database.DatabaseTransferTask.TransferPair;
import com.songoda.kingdoms.objects.land.Land;

public abstract class Manager implements Listener, Comparable<Manager> {
	
	private final Map<Class<?>, Database<?>> databases = new HashMap<>();
	protected final FileConfiguration configuration;
	protected final Kingdoms instance;
	protected final String name;
	private String[] after;
	
	protected Manager(String name, boolean listener) {
		this.name = name;
		this.instance = Kingdoms.getInstance();
		this.configuration = instance.getConfig();
		if (listener)
			Bukkit.getPluginManager().registerEvents(this, instance);
	}
	
	protected Manager(String name, boolean listener, String... after) {
		this(name, listener);
		this.after = after;
	}
	
	@SuppressWarnings("unchecked")
	protected <T> Database<T> getMySQLDatabase(Class<T> type) {
		if (databases.containsKey(type))
			return (MySQLDatabase<T>) databases.get(type);
		ConfigurationSection section = instance.getConfig().getConfigurationSection("database");
		String landTable = section.getString("land-table", "Lands");
		String address = section.getString("mysql.address", "localhost");
		String password = section.getString("mysql.password", "1234");
		String name = section.getString("mysql.name", "kingdoms");
		String user = section.getString("mysql.user", "root");
		Database<T> database = null;
		try {
			database = new MySQLDatabase<>(address, name, landTable, user, password, Land.class);
			Kingdoms.consoleMessage("MySQL connection " + address + " was a success!");
			databases.put(type, (MySQLDatabase<?>) database);
			if (configuration.getBoolean("database.transfer.mysql", false)) {
				TransferPair<T> transfer = new TransferPair<T>(database, getSQLiteDatabase(type));
				new Thread(new DatabaseTransferTask<T>(instance, transfer)).start();
			}
			return database;
		} catch (SQLException exception) {
			Kingdoms.consoleMessage("&cMySQL connection failed!");
			Kingdoms.consoleMessage("Address: " + address + " with user: " + user);
			Kingdoms.consoleMessage("Reason: " + exception.getMessage());
		} finally {
			if (database == null) {
				Kingdoms.consoleMessage("Attempting to use SQLite instead...");
				database = getSQLiteDatabase(type);
			}
		}
		return database;
	}

	@SuppressWarnings("unchecked")
	protected <T> Database<T> getSQLiteDatabase(Class<T> type) {
		if (databases.containsKey(type))
			return (SQLiteDatabase<T>) databases.get(type);
		String landTable = configuration.getString("database.land-table", "Lands");
		Database<T> database = null;
		try {
			database = new SQLiteDatabase<>("db.db", landTable, type);
			Kingdoms.consoleMessage("Using SQLite database for Land data");
			databases.put(type, database);
		} catch (ClassNotFoundException | SQLException e) {
			Kingdoms.consoleMessage("SQLite failed...");
			Kingdoms.consoleMessage("Using Yaml instead.");
			database = new YamlDatabase<T>();
		}
		return database;
	}

	@Override
	public int compareTo(Manager manager) {
		Optional<String> optional = manager.after().parallelStream()
				.filter(after -> after.equalsIgnoreCase(name))
				.findFirst();
		if (optional.isPresent())
			return 1;
		return 0;
	}
	
	public Set<String> after() {
		return Sets.newHashSet(after);
	}
	
	public String getName() {
		return name;
	}
	
	/**
	 * Used for grabbing other instances of Managers.
	 * Can't grab other managers within the constructor.
	 */
	public abstract void initalize();
	
	public abstract void onDisable();

}
