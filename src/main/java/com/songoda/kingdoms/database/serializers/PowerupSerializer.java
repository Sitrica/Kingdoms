package com.songoda.kingdoms.database.serializers;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.songoda.kingdoms.database.Serializer;
import com.songoda.kingdoms.database.handlers.PowerupHandler;
import com.songoda.kingdoms.objects.kingdom.Powerup;
import com.songoda.kingdoms.objects.kingdom.PowerupType;

public class PowerupSerializer implements Serializer<Powerup> {

	private final PowerupHandler handler;

	public PowerupSerializer() {
		this.handler = new PowerupHandler();
	}

	@Override
	public JsonElement serialize(Powerup powerup, Type type, JsonSerializationContext context) {
		JsonObject json = new JsonObject();
		JsonObject powrups = new JsonObject();
		for (PowerupType powerupType : PowerupType.values()) {
			powrups.addProperty(powerupType.name(), powerup.getLevel(powerupType));
		}
		json.add("powerups", powrups);
		return handler.serialize(powerup, json, context);
	}

	@Override
	public Powerup deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
		return handler.deserialize(null, json.getAsJsonObject(), context);
	}

}
