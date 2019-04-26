package com.songoda.kingdoms.database.handlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.songoda.kingdoms.database.serializers.LandSerializer;
import com.songoda.kingdoms.database.serializers.OfflineKingdomSerializer;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.player.OfflineKingdomPlayer;

public class OfflineKingdomPlayerHandler implements Handler<OfflineKingdomPlayer> {

	private final OfflineKingdomSerializer kingdomSerializer;
	private final LandSerializer landSerializer;

	public OfflineKingdomPlayerHandler() {
		this.kingdomSerializer = new OfflineKingdomSerializer();
		this.landSerializer = new LandSerializer();
	}

	@Override
	public JsonObject serialize(OfflineKingdomPlayer player, JsonObject json, JsonSerializationContext context) {
		json.add("kingdom", kingdomSerializer.serialize(player.getKingdom(), OfflineKingdom.class, context));
		JsonArray claims = new JsonArray();
		player.getClaims().forEach(land -> claims.add(landSerializer.serialize(land, Land.class, context)));
		json.add("claims", claims);
		return json;
	}

	@Override
	public OfflineKingdomPlayer deserialize(OfflineKingdomPlayer player, JsonObject json, JsonDeserializationContext context) {
		JsonElement kingdomElement = json.get("kingdom");
		if (kingdomElement != null && !kingdomElement.isJsonNull())
			player.setKingdom(kingdomSerializer.deserialize(kingdomElement, OfflineKingdomPlayer.class, context));
		JsonElement claimsElement = json.get("claims");
		if (claimsElement != null && !claimsElement.isJsonNull() && claimsElement.isJsonArray()) {
			JsonArray array = claimsElement.getAsJsonArray();
			array.forEach(element -> {
				Land land = landSerializer.deserialize(element, Land.class, context);
				if (land != null)
					player.addClaim(land);
			});
		}
		return player;
	}

}
