package com.songoda.kingdoms.database;

import com.google.gson.JsonSyntaxException;
import com.songoda.kingdoms.Kingdoms;

import java.io.File;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class SQLiteDatabase<T> extends Database<T> {
	
	private final Queue<PreparedStatement> saveStatements = new ArrayDeque<>();
	private Connection connection;
	private String tablename;
	private Kingdoms instance;
	private final Type type;
	private boolean busy;
	
	public SQLiteDatabase(String name, String tablename, Type type) throws SQLException, ClassNotFoundException {
		this.instance = Kingdoms.getInstance();
		this.tablename = tablename;
		this.type = type;
		String url = "jdbc:sqlite:" + instance.getDataFolder().getAbsolutePath() + File.separator + name;
		Class.forName("org.sqlite.JDBC");
		connection = DriverManager.getConnection(url);
		if (connection != null)
			initTable();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public T load(String key, T def) {
		T result = def;
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT `data` FROM %table WHERE `id` = ?;".replace("%table", tablename));
			statement.setString(1, key);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				String ser = rs.getString("data");
				try {
					result = (T) deserialize(ser, type);
				} catch (JsonSyntaxException e) {
					e.printStackTrace();
					return def;
				}
				if (result == null)
					return def;
			}
			statement.close();
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public void save(String key, T value) {
		try {
			if (value != null) {
				PreparedStatement statement = connection.prepareStatement("REPLACE INTO %table (`id`,`data`) VALUES(?,?);".replace("%table", tablename));
				statement.setString(1, key);
				String json = serialize(value, type);
				statement.setString(2, json);
				if (!busy){
					DatabaseSave(statement);
				} else {
					saveStatements.add(statement);
				}
			} else {
				PreparedStatement statement = connection.prepareStatement("DELETE FROM %table WHERE id = ?".replace("%table", tablename));
				statement.setString(1, key);
				if (!busy){
					DatabaseSave(statement);
				} else {
					saveStatements.add(statement);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void DatabaseSave(PreparedStatement statement){
		busy = true;
		try {
			statement.executeUpdate();
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (!saveStatements.isEmpty()) {
			DatabaseSave(saveStatements.poll());
		}
	}
	
	@Override
	public boolean has(String key) {
		boolean result = false;
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT `id` FROM %table WHERE `id` = ?;".replace("%table", tablename));
			statement.setString(1, key);
			ResultSet rs = statement.executeQuery();
			result = rs.next();
			rs.close();
			statement.close();
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public void clear() {
		try {
			PreparedStatement statement = connection.prepareStatement("DELETE FROM %table;".replace("%table", tablename));
			statement.executeQuery();
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Set<String> getKeys() {
		Set<String> tempset = new HashSet<>();
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT `id` FROM %table;".replace("%table", tablename));
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				tempset.add(rs.getString("id"));
			}
			rs.close();
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return tempset;
	}

	private void initTable() throws SQLException {
		PreparedStatement stmt = connection.prepareStatement("CREATE TABLE IF NOT EXISTS %table (`id` CHAR(36) PRIMARY KEY, `data` TEXT);".replace("%table", tablename));
		stmt.executeUpdate();
		stmt.close();
	}

}
