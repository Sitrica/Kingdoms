package com.songoda.kingdoms.database.handlers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.songoda.kingdoms.database.serializers.OfflineKingdomSerializer;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.kingdom.Powerup;
import com.songoda.kingdoms.objects.kingdom.PowerupType;

public class PowerupHandler implements Handler<Powerup> {

	public final OfflineKingdomSerializer kingdomSerializer; 

	public PowerupHandler() {
		this.kingdomSerializer = new OfflineKingdomSerializer();
	}

	@Override
	public JsonObject serialize(Powerup powerup, JsonObject json, JsonSerializationContext context) {
		json.add("kingdom", kingdomSerializer.serialize(powerup.getKingdom(), OfflineKingdom.class, context));
		return json;
	}

	@Override
	public Powerup deserialize(Powerup powerup, JsonObject json, JsonDeserializationContext context) {
		JsonElement kingdomElement = json.get("kingdom");
		if (kingdomElement == null || kingdomElement.isJsonNull())
			return null;
		OfflineKingdom kingdom = kingdomSerializer.deserialize(kingdomElement, OfflineKingdom.class, context);
		if (kingdom == null)
			return null;
		powerup = new Powerup(kingdom);
		JsonElement powerupsElement = json.get("powerups");
		if (powerupsElement == null || powerupsElement.isJsonNull() || !powerupsElement.isJsonObject())
			return powerup;
		JsonObject powerupsObject = powerupsElement.getAsJsonObject();
		for (PowerupType powerupType : PowerupType.values()) {
			JsonElement element = powerupsObject.get(powerupType.name());
			if (element == null || element.isJsonNull())
				continue;
			powerup.setLevel(element.getAsInt(), powerupType);
		}
		return powerup;
	}

}
