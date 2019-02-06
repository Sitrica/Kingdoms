package com.songoda.kingdoms.database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.songoda.kingdoms.database.serializers.*;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Set;

public abstract class Database<T> {
	
	private final GsonBuilder builder = new GsonBuilder()
			.registerTypeAdapter(ItemStack[].class, new ItemStackArraySerializer())
			.registerTypeAdapter(ItemStack.class, new ItemStackSerializer())
			.registerTypeAdapter(Location.class, new LocationSerializer())
			.excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.STATIC)
			.enableComplexMapKeySerialization()
			.serializeNulls();
	
	public void registerSerializer(Class<?> clazz, Object object) {
		synchronized (builder) {
			builder.registerTypeAdapter(clazz, object);
		}
	}
	
	public abstract void save(String key, T value);
	
	public abstract T get(String key, T def);
	
	public T get(String key) {
		return get(key, null);
	}
	
	public void delete(String key) {
		save(key, null);
	}
	
	public abstract boolean has(String key);
	
	public abstract Set<String> getKeys();
	
	public abstract void clear();
	
	private Gson gson;
	
	public String serialize(Object object, Type type) {
		if (gson == null)
			gson = builder.create();
		return gson.toJson(object, type);
	}
	
	public Object deserialize(String serialized, Type type) {
		if (gson == null)
			gson = builder.create();
		return gson.fromJson(serialized, type);
	}

}
