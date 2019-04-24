package com.songoda.kingdoms.database.serializers;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.songoda.kingdoms.database.OfflineKingdomSerializer;
import com.songoda.kingdoms.database.Serializer;
import com.songoda.kingdoms.objects.kingdom.MiscUpgrade;
import com.songoda.kingdoms.objects.kingdom.MiscUpgradeType;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;

public class MiscUpgradeSerializer implements Serializer<MiscUpgrade> {

	private final OfflineKingdomSerializer kingdomSerializer;

	public MiscUpgradeSerializer(OfflineKingdomSerializer kingdomSerializer) {
		this.kingdomSerializer = kingdomSerializer;
	}

	@Override
	public JsonElement serialize(MiscUpgrade upgrade, Type type, JsonSerializationContext context) {
		JsonObject json = new JsonObject();
		json.add("kingdom", kingdomSerializer.serialize(upgrade.getKingdom(), OfflineKingdom.class, context));
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
		return json;
	}

	@Override
	public MiscUpgrade deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
		JsonElement kingdomElement = object.get("kingdom");
		if (kingdomElement == null || kingdomElement.isJsonNull())
			return null;
		OfflineKingdom kingdom = kingdomSerializer.deserialize(kingdomElement, OfflineKingdom.class, context);
		if (kingdom == null)
			return null;
		MiscUpgrade upgrade = new MiscUpgrade(kingdom);
		JsonElement boughtElement = object.get("bought");
		if (boughtElement == null || boughtElement.isJsonNull() || !boughtElement.isJsonObject())
			return upgrade;
		JsonObject boughtObject = boughtElement.getAsJsonObject();
		for (MiscUpgradeType upgradeType : MiscUpgradeType.values()) {
			JsonElement element = boughtObject.get(upgradeType.name());
			if (element == null || element.isJsonNull())
				continue;
			upgrade.setBought(upgradeType, element.getAsBoolean());
		}
		JsonElement enabledElement = object.get("enabled");
		if (enabledElement == null || enabledElement.isJsonNull() || !enabledElement.isJsonObject())
			return upgrade;
		JsonObject enabledObject = enabledElement.getAsJsonObject();
		for (MiscUpgradeType upgradeType : MiscUpgradeType.values()) {
			JsonElement element = enabledObject.get(upgradeType.name());
			if (element == null || element.isJsonNull())
				continue;
			upgrade.setEnabled(upgradeType, element.getAsBoolean());
		}
		return upgrade;
	}

}
