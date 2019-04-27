package com.songoda.kingdoms.database.serializers;

import java.lang.reflect.Type;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.database.Serializer;
import com.songoda.kingdoms.manager.managers.KingdomManager;
import com.songoda.kingdoms.manager.managers.RankManager;
import com.songoda.kingdoms.manager.managers.RankManager.Rank;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.player.OfflineKingdomPlayer;

public class OfflineKingdomPlayerSerializer implements Serializer<OfflineKingdomPlayer> {

	private final KingdomManager kingdomManager;
	private final RankManager rankManager;

	public OfflineKingdomPlayerSerializer() {
		this.kingdomManager = Kingdoms.getInstance().getManager("kingdom", KingdomManager.class);
		this.rankManager = Kingdoms.getInstance().getManager("rank", RankManager.class);
	}

	@Override
	public JsonElement serialize(OfflineKingdomPlayer player, Type type, JsonSerializationContext context) {
		JsonObject json = new JsonObject();
		json.addProperty("rank", player.getRank().getName());
		json.addProperty("uuid", player.getUniqueId() + "");
		OfflineKingdom kingdom = player.getKingdom();
		if (kingdom != null)
			json.addProperty("kingdom", kingdom.getUniqueId() + "");
		JsonArray claims = new JsonArray();
		LandSerializer landSerializer = new LandSerializer();
		player.getClaims().forEach(land -> claims.add(landSerializer.serialize(land, Land.class, context)));
		json.add("claims", claims);
		return json;
	}

	@Override
	public OfflineKingdomPlayer deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
		JsonElement uuidElement = object.get("uuid");
		if (uuidElement == null || uuidElement.isJsonNull())
			return null;
		UUID uuid = UUID.fromString(uuidElement.getAsString());
		if (uuid == null)
			return null;
		OfflineKingdomPlayer player = new OfflineKingdomPlayer(uuid);
		JsonElement rankElement = object.get("rank");
		if (rankElement != null && !rankElement.isJsonNull()) {
			Rank rank = rankManager.getRank(uuidElement.getAsString()).orElse(rankManager.getDefaultRank());
			player.setRank(rank);
		}
		JsonElement kingdomElement = object.get("kingdom");
		if (kingdomElement != null && !kingdomElement.isJsonNull()) {
			UUID kingdomUuid = UUID.fromString(kingdomElement.getAsString());
			if (uuid != null) {
				OfflineKingdom kingdom = kingdomManager.getKingdom(kingdomUuid);
				if (kingdom != null)
					player.setKingdom(kingdom);
			}
		}
		JsonElement claimsElement = object.get("claims");
		if (claimsElement != null && !claimsElement.isJsonNull() && claimsElement.isJsonArray()) {
			LandSerializer landSerializer = new LandSerializer();
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
