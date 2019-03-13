package com.songoda.kingdoms.database.serializers;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import com.songoda.kingdoms.database.Serializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

public class UUIDListSerializer implements Serializer<List<UUID>> {

	@Override
	public JsonElement serialize(List<UUID> obj, Type type, JsonSerializationContext context) {
		JsonArray array = new JsonArray();
		for (UUID uuid : obj)
			array.add(context.serialize(uuid));
		return array;
	}

	@Override
	public List<UUID> deserialize(JsonElement obj, Type type, JsonDeserializationContext context) throws JsonParseException {
		JsonArray array = (JsonArray) obj;
		List<UUID> uuids = new ArrayList<>();
		Iterator<JsonElement> iter = array.iterator();
		while(iter.hasNext()) {
			UUID uuid = context.deserialize(iter.next(), UUID.class);
			uuids.add(uuid);
		}
		return uuids;
	}

}
