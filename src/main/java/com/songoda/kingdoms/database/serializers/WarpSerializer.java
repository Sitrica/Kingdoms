package com.songoda.kingdoms.database.serializers;

import java.lang.reflect.Type;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.songoda.kingdoms.database.Serializer;
import com.songoda.kingdoms.objects.structures.WarpPad.Warp;

public class WarpSerializer implements Serializer<Warp> {

	@Override
	public JsonElement serialize(Warp warp, Type type, JsonSerializationContext context) {
		JsonObject json = new JsonObject();
		Location location = warp.getLocation();
		json.addProperty("name", warp.getName());
		json.addProperty("world", location.getWorld().getName());
		json.addProperty("x", location.getX());
		json.addProperty("y", location.getY());
		json.addProperty("z", location.getZ());
		return json;
	}

	@Override
	public Warp deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
		JsonElement worldElement = object.get("world");
		if (worldElement == null || worldElement.isJsonNull())
			return null;
		World world = Bukkit.getWorld(worldElement.getAsString());
		if (world == null)
			return null;
		double x = object.get("x").getAsDouble();
		double y = object.get("y").getAsDouble();
		double z = object.get("z").getAsDouble();
		Location location = new Location(world, x, y, z);
		JsonElement name = object.get("name");
		if (name == null || name.isJsonNull())
			return null;
		return new Warp(name.getAsString(), location);
	}

}
