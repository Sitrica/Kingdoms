package com.songoda.kingdoms.database.handlers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.songoda.kingdoms.database.serializers.OfflineKingdomSerializer;
import com.songoda.kingdoms.objects.kingdom.MiscUpgrade;
import com.songoda.kingdoms.objects.kingdom.MiscUpgradeType;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;

public class MiscUpgradeHandler implements Handler<MiscUpgrade> {

	private final OfflineKingdomSerializer kingdomSerializer;

	public MiscUpgradeHandler() {
		this.kingdomSerializer = new OfflineKingdomSerializer();
	}

	@Override
	public JsonObject serialize(MiscUpgrade upgrade, JsonObject json, JsonSerializationContext context) {
		json.add("kingdom", kingdomSerializer.serialize(upgrade.getKingdom(), OfflineKingdom.class, context));
		return json;
	}

	@Override
	public MiscUpgrade deserialize(MiscUpgrade upgrade, JsonObject json, JsonDeserializationContext context) {
		JsonElement kingdomElement = json.get("kingdom");
		if (kingdomElement == null || kingdomElement.isJsonNull())
			return null;
		OfflineKingdom kingdom = kingdomSerializer.deserialize(kingdomElement, OfflineKingdom.class, context);
		if (kingdom == null)
			return null;
		upgrade = new MiscUpgrade(kingdom);
		JsonElement boughtElement = json.get("bought");
		if (boughtElement == null || boughtElement.isJsonNull() || !boughtElement.isJsonObject())
			return upgrade;
		JsonObject boughtObject = boughtElement.getAsJsonObject();
		for (MiscUpgradeType upgradeType : MiscUpgradeType.values()) {
			JsonElement element = boughtObject.get(upgradeType.name());
			if (element == null || element.isJsonNull())
				continue;
			upgrade.setBought(upgradeType, element.getAsBoolean());
		}
		JsonElement enabledElement = json.get("enabled");
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
