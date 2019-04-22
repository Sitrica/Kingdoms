package com.songoda.kingdoms.database.serializers;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.database.Serializer;
import com.songoda.kingdoms.manager.managers.RankManager;
import com.songoda.kingdoms.manager.managers.RankManager.Rank;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.player.OfflineKingdomPlayer;
import java.lang.reflect.Type;
import java.util.UUID;

public class OfflineKingdomPlayerSerializer implements Serializer<OfflineKingdomPlayer> {

	private final LandSerializer landSerializer;
	private final RankManager rankManager;

	public OfflineKingdomPlayerSerializer() {
		this.rankManager = Kingdoms.getInstance().getManager("rank", RankManager.class);
		this.landSerializer = new LandSerializer();
	}

	@Override
	public JsonElement serialize(OfflineKingdomPlayer player, Type type, JsonSerializationContext context) {
		JsonObject object = new JsonObject();
		object.addProperty("uuid", player.getUniqueId() + "");
		object.addProperty("rank", player.getRank().getName());
		JsonArray claims = new JsonArray();
		player.getClaims().forEach(land -> claims.add(landSerializer.serialize(land, Land.class, context)));
		object.add("claims", claims);
		return object;
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
		if (rankElement == null || rankElement.isJsonNull())
			return null;
		Rank rank = rankManager.getRank(uuidElement.getAsString()).orElse(rankManager.getDefaultRank());
		player.setRank(rank);
		JsonElement claimsElement = object.get("claims");
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
