package com.songoda.kingdoms.manager;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;

import com.google.common.collect.Sets;
import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.database.Database;
import com.songoda.kingdoms.database.DatabaseTransferTask;
import com.songoda.kingdoms.database.MySQLDatabase;
import com.songoda.kingdoms.database.YamlDatabase;
import com.songoda.kingdoms.database.DatabaseTransferTask.TransferPair;
import com.songoda.kingdoms.database.H2Database;

public abstract class Manager implements Listener, Comparable<Manager> {

	private final Map<Class<?>, Database<?>> databases = new HashMap<>();
	protected final FileConfiguration configuration;
	protected final Kingdoms instance;
	private final boolean listener;
	private final String name;
	private String[] after;

	protected Manager(String name, boolean listener) {
		this.name = name;
		this.listener = listener;
		this.instance = Kingdoms.getInstance();
		this.configuration = instance.getConfig();
	}

	protected Manager(String name, boolean listener, String... after) {
		this(name, listener);
		this.after = after;
	}

	public boolean hasListener() {
		return listener;
	}

	@SuppressWarnings("unchecked")
	protected <T> Database<T> getMySQLDatabase(String table, Class<T> type) {
		if (databases.containsKey(type))
			return (MySQLDatabase<T>) databases.get(type);
		ConfigurationSection section = instance.getConfig().getConfigurationSection("database");
		String address = section.getString("mysql.address", "localhost");
		String password = section.getString("mysql.password", "1234");
		String name = section.getString("mysql.name", "kingdoms");
		String user = section.getString("mysql.user", "root");
		Database<T> database = null;
		try {
			database = new MySQLDatabase<>(address, name, table, user, password, type);
			Kingdoms.consoleMessage("MySQL connection " + address + " was a success!");
			databases.put(type, (MySQLDatabase<?>) database);
			if (configuration.getBoolean("database.transfer.mysql", false)) {
				TransferPair<T> transfer = new TransferPair<T>(database, getFileDatabase(table, type));
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
				database = getFileDatabase(table, type);
			}
		}
		return database;
	}

	@SuppressWarnings("unchecked")
	protected <T> Database<T> getFileDatabase(String table, Class<T> type) {
		if (databases.containsKey(type))
			return (H2Database<T>) databases.get(type);
		Database<T> database = null;
		try {
			//TODO make a database that reads db.db SQLite database if it exists and convert that old data.
			database = new H2Database<>(table, type);
			Kingdoms.consoleMessage("Using H2 database for " + type.getSimpleName() + " data");
			databases.put(type, database);
		} catch (ClassNotFoundException | SQLException e) {
			Kingdoms.consoleMessage("H2 failed...");
			Kingdoms.consoleMessage("Using Yaml instead.");
			database = new YamlDatabase<T>();
		}
		return database;
	}

	@Override
	public int compareTo(Manager manager) {
		if (manager.after().contains(name))
			return 1; // This object is greater than manager.
		return 0;
	}
	
	public Set<String> after() {
		if (after == null)
			return Sets.newHashSet();
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
