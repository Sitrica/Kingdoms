package com.songoda.kingdoms.database.handlers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.songoda.kingdoms.database.serializers.OfflineKingdomSerializer;
import com.songoda.kingdoms.objects.kingdom.DefenderInfo;
import com.songoda.kingdoms.objects.kingdom.DefenderUpgrade;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;

public class DefenderInfoHandler implements Handler<DefenderInfo> {

	private final OfflineKingdomSerializer kingdomSerializer;

	public DefenderInfoHandler() {
		this.kingdomSerializer = new OfflineKingdomSerializer();
	}

	@Override
	public JsonObject serialize(DefenderInfo upgrade, JsonObject json, JsonSerializationContext context) {
		json.add("kingdom", kingdomSerializer.serialize(upgrade.getKingdom(), OfflineKingdom.class, context));
		return json;
	}

	@Override
	public DefenderInfo deserialize(DefenderInfo object, JsonObject json, JsonDeserializationContext context) {
		JsonElement kingdomElement = json.get("kingdom");
		if (kingdomElement == null || kingdomElement.isJsonNull())
			return null;
		OfflineKingdom kingdom = kingdomSerializer.deserialize(kingdomElement, OfflineKingdom.class, context);
		if (kingdom == null)
			return null;
		DefenderInfo info = new DefenderInfo(kingdom);
		JsonElement upgradesElement = json.get("upgrades");
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
