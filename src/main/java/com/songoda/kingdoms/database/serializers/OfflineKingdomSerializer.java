package com.songoda.kingdoms.database.serializers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.songoda.kingdoms.database.Serializer;
import com.songoda.kingdoms.objects.kingdom.MiscUpgrade;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.kingdom.Powerup;
import com.songoda.kingdoms.objects.player.OfflineKingdomPlayer;
import java.lang.reflect.Type;
import java.util.UUID;

import org.bukkit.Location;

public class OfflineKingdomSerializer implements Serializer<OfflineKingdom> {

	private final OfflineKingdomPlayerSerializer playerSerializer;
	private final MiscUpgradeSerializer miscUpgradeSerializer;
	private final LocationSerializer locationSerializer;
	private final PowerupSerializer powerupSerializer;
	private final LandSerializer landSerializer;

	public OfflineKingdomSerializer() {
		this.playerSerializer = new OfflineKingdomPlayerSerializer();
		this.miscUpgradeSerializer = new MiscUpgradeSerializer();
		this.locationSerializer = new LocationSerializer();
		this.powerupSerializer = new PowerupSerializer();
		this.landSerializer = new LandSerializer();
	}

	@Override
	public JsonElement serialize(OfflineKingdom kingdom, Type type, JsonSerializationContext context) {
		JsonObject json = new JsonObject();
		json.addProperty("lore", kingdom.getLore());
		json.addProperty("name", kingdom.getName());
		json.addProperty("uuid", kingdom.getUniqueId() + "");
		json.add("spawn", locationSerializer.serialize(kingdom.getSpawn(), Location.class, context));
		json.add("powerup", powerupSerializer.serialize(kingdom.getPowerup(), Powerup.class, context));
		json.add("king", playerSerializer.serialize(kingdom.getKing(), OfflineKingdomPlayer.class, context));
		json.add("nexus", locationSerializer.serialize(kingdom.getNexusLocation(), Location.class, context));
		json.add("misc-upgrades", miscUpgradeSerializer.serialize(kingdom.getMiscUpgrades(), MiscUpgrade.class, context));
		/*JsonArray claims = new JsonArray();
		player.getClaims().forEach(land -> claims.add(landSerializer.serialize(land, Land.class, context)));
		object.add("claims", claims);
		*/
		return json;
	}

	@Override
	public OfflineKingdom deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
		JsonElement uuidElement = object.get("uuid");
		if (uuidElement == null || uuidElement.isJsonNull())
			return null;
		UUID uuid = UUID.fromString(uuidElement.getAsString());
		if (uuid == null)
			return null;
		JsonElement kingElement = object.get("king");
		if (kingElement == null || kingElement.isJsonNull())
			return null;
		OfflineKingdomPlayer king = playerSerializer.deserialize(kingElement, OfflineKingdomPlayer.class, context);
		if (king == null)
			return null;
		OfflineKingdom kingdom = new OfflineKingdom(uuid, king);
		JsonElement powerupElement = object.get("powerup");
		if (powerupElement != null && !powerupElement.isJsonNull()) {
			Powerup powerup = powerupSerializer.deserialize(powerupElement, Powerup.class, context);
			if (powerup != null)
				kingdom.setPowerup(powerup);
		}
		JsonElement nameElement = object.get("name");
		if (nameElement != null && !nameElement.isJsonNull())
			kingdom.setName(nameElement.getAsString());
		JsonElement loreElement = object.get("lore");
		if (loreElement != null && !loreElement.isJsonNull())
			kingdom.setLore(loreElement.getAsString());
		JsonElement spawnElement = object.get("spawn");
		if (spawnElement != null && !spawnElement.isJsonNull())
			kingdom.setSpawn(locationSerializer.deserialize(spawnElement, Location.class, context));
		JsonElement nexusElement = object.get("nexus");
		if (nexusElement != null && !nexusElement.isJsonNull())
			kingdom.setNexusLocation(locationSerializer.deserialize(nexusElement, Location.class, context));
		JsonElement miscUpgradesElement = object.get("misc-upgrades");
		if (miscUpgradesElement != null && !miscUpgradesElement.isJsonNull())
			kingdom.setMiscUpgrades(miscUpgradeSerializer.deserialize(miscUpgradesElement, MiscUpgrade.class, context));
		/*
		JsonElement claimsElement = object.get("claims");
		if (claimsElement != null && !claimsElement.isJsonNull() && claimsElement.isJsonArray()) {
			JsonArray array = claimsElement.getAsJsonArray();
			array.forEach(element -> {
				Land land = landSerializer.deserialize(element, Land.class, context);
				if (land != null)
					player.addClaim(land);
			});
		}
		*/
		return kingdom;
	}

}
