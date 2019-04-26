package com.songoda.kingdoms.database.serializers;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.songoda.kingdoms.database.Serializer;
import com.songoda.kingdoms.database.handlers.MiscUpgradeHandler;
import com.songoda.kingdoms.objects.kingdom.MiscUpgrade;
import com.songoda.kingdoms.objects.kingdom.MiscUpgradeType;

public class MiscUpgradeSerializer implements Serializer<MiscUpgrade> {

	private final MiscUpgradeHandler handler;

	public MiscUpgradeSerializer() {
		this.handler = new MiscUpgradeHandler();
	}

	@Override
	public JsonElement serialize(MiscUpgrade upgrade, Type type, JsonSerializationContext context) {
		JsonObject json = new JsonObject();
		JsonObject bought = new JsonObject();
		for (MiscUpgradeType upgradeType : MiscUpgradeType.values()) {
			bought.addProperty(upgradeType.name(), upgrade.hasBought(upgradeType));
		}
		json.add("bought", bought);
		JsonObject enabled = new JsonObject();
		for (MiscUpgradeType upgradeType : MiscUpgradeType.values()) {
			enabled.addProperty(upgradeType.name(), upgrade.isEnabled(upgradeType));
		}
		json.add("enabled", enabled);
		return handler.serialize(upgrade, json, context);
	}

	@Override
	public MiscUpgrade deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
		return handler.deserialize(null, json.getAsJsonObject(), context);
	}

}
