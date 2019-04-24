package com.songoda.kingdoms.database.serializers;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.songoda.kingdoms.database.OfflineKingdomSerializer;
import com.songoda.kingdoms.database.Serializer;
import com.songoda.kingdoms.objects.kingdom.DefenderInfo;
import com.songoda.kingdoms.objects.kingdom.DefenderUpgrade;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;

public class DefenderInfoSerializer implements Serializer<DefenderInfo> {

	private final OfflineKingdomSerializer kingdomSerializer;

	public DefenderInfoSerializer(OfflineKingdomSerializer kingdomSerializer) {
		this.kingdomSerializer = kingdomSerializer;
	}

	@Override
	public JsonElement serialize(DefenderInfo upgrade, Type type, JsonSerializationContext context) {
		JsonObject json = new JsonObject();
		json.add("kingdom", kingdomSerializer.serialize(upgrade.getKingdom(), OfflineKingdom.class, context));
		JsonObject upgrades = new JsonObject();
		for (DefenderUpgrade upgradeType : DefenderUpgrade.values()) {
			upgrades.addProperty(upgradeType.name(), upgrade.getUpgradeLevel(upgradeType));
		}
		json.add("upgrades", upgrades);
		return json;
	}

	@Override
	public DefenderInfo deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
		JsonElement kingdomElement = object.get("kingdom");
		if (kingdomElement == null || kingdomElement.isJsonNull())
			return null;
		OfflineKingdom kingdom = kingdomSerializer.deserialize(kingdomElement, OfflineKingdom.class, context);
		if (kingdom == null)
			return null;
		DefenderInfo info = new DefenderInfo(kingdom);
		JsonElement upgradesElement = object.get("upgrades");
		if (upgradesElement == null || upgradesElement.isJsonNull() || !upgradesElement.isJsonObject())
			return info;
		JsonObject upgradesObject = upgradesElement.getAsJsonObject();
		for (DefenderUpgrade upgradeType : DefenderUpgrade.values()) {
			JsonElement element = upgradesObject.get(upgradeType.name());
			if (element == null || element.isJsonNull())
				continue;
			info.setUpgradeLevel(upgradeType, element.getAsInt());
		}
		return info;
	}

}
