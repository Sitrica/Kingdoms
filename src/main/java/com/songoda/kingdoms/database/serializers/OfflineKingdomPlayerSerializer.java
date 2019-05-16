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
import com.songoda.kingdoms.manager.managers.LandManager;
import com.songoda.kingdoms.manager.managers.RankManager;
import com.songoda.kingdoms.manager.managers.RankManager.Rank;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.player.OfflineKingdomPlayer;

public class OfflineKingdomPlayerSerializer implements Serializer<OfflineKingdomPlayer> {

	@Override
	public JsonElement serialize(OfflineKingdomPlayer player, Type type, JsonSerializationContext context) {
		JsonObject json = new JsonObject();
		Rank rank = player.getRank();
		if (rank != null)
			json.addProperty("rank", rank.getName());
		json.addProperty("uuid", player.getUniqueId() + "");
		OfflineKingdom kingdom = player.getKingdom();
		if (kingdom != null)
			json.addProperty("kingdom", kingdom.getName());
		JsonArray claims = new JsonArray();
		LandSerializer landSerializer = new LandSerializer();
		player.getClaims().forEach(land -> claims.add(landSerializer.serialize(land, Land.class, context)));
		json.add("claims", claims);
		return json;
	}

	@Override
	public OfflineKingdomPlayer deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
		Kingdoms instance = Kingdoms.getInstance();
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
			RankManager rankManager = instance.getManager("rank", RankManager.class);
			Rank rank = rankManager.getRank(rankElement.getAsString()).orElse(rankManager.getDefaultRank());
			player.setRank(rank);
		}
		JsonElement kingdomElement = object.get("kingdom");
		if (kingdomElement != null && !kingdomElement.isJsonNull())
			player.setKingdom(kingdomElement.getAsString());
		JsonElement claimsElement = object.get("claims");
		if (claimsElement != null && !claimsElement.isJsonNull() && claimsElement.isJsonArray()) {
			LandSerializer landSerializer = new LandSerializer();
			JsonArray array = claimsElement.getAsJsonArray();
			LandManager landManager = instance.getManager("land", LandManager.class);
			array.forEach(element -> {
				Land land = landSerializer.deserialize(element, Land.class, context);
				if (land != null)
					player.addClaim(landManager.new LandInfo(land));
			});
		}
		return player;
	}

}
