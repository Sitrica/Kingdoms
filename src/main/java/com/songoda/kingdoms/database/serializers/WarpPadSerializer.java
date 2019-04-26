package com.songoda.kingdoms.database.serializers;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.songoda.kingdoms.database.Serializer;
import com.songoda.kingdoms.database.handlers.WarpPadHandler;
import com.songoda.kingdoms.objects.structures.WarpPad;

public class WarpPadSerializer implements Serializer<WarpPad> {

	private final WarpPadHandler handler;

	public WarpPadSerializer() {
		this.handler = new WarpPadHandler();
	}

	@Override
	public JsonElement serialize(WarpPad warp, Type type, JsonSerializationContext context) {
		JsonObject json = new JsonObject();
		json.addProperty("name", warp.getName());
		return handler.serialize(warp, json, context);
	}

	@Override
	public WarpPad deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
		return handler.deserialize(null, json.getAsJsonObject(), context);
	}

}
