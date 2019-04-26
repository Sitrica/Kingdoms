package com.songoda.kingdoms.database.serializers;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.songoda.kingdoms.database.Serializer;
import com.songoda.kingdoms.database.handlers.KingdomChestHandler;
import com.songoda.kingdoms.objects.kingdom.KingdomChest;

public class KingdomChestSerializer implements Serializer<KingdomChest> {

	private final KingdomChestHandler handler;

	public KingdomChestSerializer() {
		this.handler = new KingdomChestHandler();
	}

	@Override
	public JsonElement serialize(KingdomChest chest, Type type, JsonSerializationContext context) {
		return handler.serialize(chest, new JsonObject(), context);
	}

	@Override
	public KingdomChest deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
		return handler.deserialize(null, json.getAsJsonObject(), context);
	}

}
