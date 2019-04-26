package com.songoda.kingdoms.database.serializers;

import java.lang.reflect.Type;

import org.bukkit.Location;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.songoda.kingdoms.database.Serializer;
import com.songoda.kingdoms.database.handlers.StructureHandler;
import com.songoda.kingdoms.objects.structures.Structure;

public class StructureSerializer implements Serializer<Structure> {

	private final StructureHandler handler;

	public StructureSerializer() {
		this.handler = new StructureHandler();
	}

	@Override
	public JsonElement serialize(Structure structure, Type type, JsonSerializationContext context) {
		JsonObject json = new JsonObject();
		Location location = structure.getLocation();
		json.addProperty("type", structure.getType().name());
		json.addProperty("world", location.getWorld().getName());
		json.addProperty("x", location.getX());
		json.addProperty("y", location.getY());
		json.addProperty("z", location.getZ());
		return json;
	}

	@Override
	public Structure deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
		return handler.deserialize(null, json.getAsJsonObject(), context);
	}

}
