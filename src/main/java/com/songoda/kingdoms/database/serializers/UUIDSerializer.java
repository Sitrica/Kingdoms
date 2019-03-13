package com.songoda.kingdoms.database.serializers;

import java.lang.reflect.Type;
import java.util.UUID;

import com.songoda.kingdoms.database.Serializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;

public class UUIDSerializer implements Serializer<UUID> {

	@Override
	public JsonElement serialize(UUID obj, Type type, JsonSerializationContext context) {
		return new JsonPrimitive(obj.toString());
	}

	@Override
	public UUID deserialize(JsonElement obj, Type type, JsonDeserializationContext context) throws JsonParseException {
		return UUID.fromString(obj.getAsString());
	}

}
