package com.songoda.kingdoms.database.serializers;

import java.lang.reflect.Type;
import java.util.UUID;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.songoda.kingdoms.database.Serializer;
import com.songoda.kingdoms.database.handlers.OfflineKingdomHandler;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.player.OfflineKingdomPlayer;

public class OfflineKingdomSerializer implements Serializer<OfflineKingdom> {

	private final OfflineKingdomPlayerSerializer playerSerializer;
	private final OfflineKingdomHandler handler;

	public OfflineKingdomSerializer() {
		this.playerSerializer = new OfflineKingdomPlayerSerializer();
		this.handler = new OfflineKingdomHandler();
	}

	@Override
	public JsonElement serialize(OfflineKingdom kingdom, Type type, JsonSerializationContext context) {
		JsonObject json = new JsonObject();
		json.addProperty("lore", kingdom.getLore());
		json.addProperty("name", kingdom.getName());
		json.addProperty("neutral", kingdom.isNeutral());
		json.addProperty("invaded", kingdom.hasInvaded());
		json.addProperty("uuid", kingdom.getUniqueId() + "");
		json.addProperty("max-members", kingdom.getMaxMembers());
		json.addProperty("first-claim", kingdom.hasUsedFirstClaim());
		json.addProperty("resource-points", kingdom.getResourcePoints());
		json.addProperty("invasion-cooldown", kingdom.getInvasionCooldown());
		return handler.serialize(kingdom, json, context);
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
		JsonElement nameElement = object.get("name");
		if (nameElement != null && !nameElement.isJsonNull())
			kingdom.setName(nameElement.getAsString());
		JsonElement loreElement = object.get("lore");
		if (loreElement != null && !loreElement.isJsonNull())
			kingdom.setLore(loreElement.getAsString());
		JsonElement neutralElement = object.get("neutral");
		if (neutralElement != null && !neutralElement.isJsonNull())
			kingdom.setNeutral(neutralElement.getAsBoolean());
		JsonElement firstElement = object.get("first-claim");
		if (firstElement != null && !firstElement.isJsonNull())
			kingdom.setUsedFirstClaim(firstElement.getAsBoolean());
		JsonElement invadedElement = object.get("invaded");
		if (invadedElement != null && !invadedElement.isJsonNull())
			kingdom.setInvaded(invadedElement.getAsBoolean());
		JsonElement pointsElement = object.get("resource-points");
		if (pointsElement != null && !pointsElement.isJsonNull())
			kingdom.setResourcePoints(pointsElement.getAsLong());
		JsonElement invasionElement = object.get("invasion-cooldown");
		if (invasionElement != null && !invasionElement.isJsonNull())
			kingdom.setInvasionCooldown(invasionElement.getAsLong());
		JsonElement maxElement = object.get("max-members");
		if (maxElement != null && !maxElement.isJsonNull())
			kingdom.setMaxMembers(maxElement.getAsInt());
		return handler.deserialize(kingdom, object, context);
	}

}
