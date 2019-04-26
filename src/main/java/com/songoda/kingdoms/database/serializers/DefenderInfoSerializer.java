package com.songoda.kingdoms.database.serializers;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.songoda.kingdoms.database.Serializer;
import com.songoda.kingdoms.database.handlers.DefenderInfoHandler;
import com.songoda.kingdoms.objects.kingdom.DefenderInfo;
import com.songoda.kingdoms.objects.kingdom.DefenderUpgrade;

public class DefenderInfoSerializer implements Serializer<DefenderInfo> {

	private final DefenderInfoHandler handler;

	public DefenderInfoSerializer() {
		this.handler = new DefenderInfoHandler();
	}

	@Override
	public JsonElement serialize(DefenderInfo upgrade, Type type, JsonSerializationContext context) {
		JsonObject json = new JsonObject();
		JsonObject upgrades = new JsonObject();
		for (DefenderUpgrade upgradeType : DefenderUpgrade.values()) {
			upgrades.addProperty(upgradeType.name(), upgrade.getUpgradeLevel(upgradeType));
		}
		json.add("upgrades", upgrades);
		return handler.serialize(upgrade, json, context);
	}

	@Override
	public DefenderInfo deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
		return handler.deserialize(null, json.getAsJsonObject(), context);
	}

}
