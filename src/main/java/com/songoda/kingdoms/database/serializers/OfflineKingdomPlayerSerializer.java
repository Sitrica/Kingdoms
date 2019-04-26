package com.songoda.kingdoms.database.serializers;

import java.lang.reflect.Type;
import java.util.UUID;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.database.Serializer;
import com.songoda.kingdoms.database.handlers.OfflineKingdomPlayerHandler;
import com.songoda.kingdoms.manager.managers.RankManager;
import com.songoda.kingdoms.manager.managers.RankManager.Rank;
import com.songoda.kingdoms.objects.player.OfflineKingdomPlayer;

public class OfflineKingdomPlayerSerializer implements Serializer<OfflineKingdomPlayer> {

	private final OfflineKingdomPlayerHandler handler;
	private final RankManager rankManager;

	public OfflineKingdomPlayerSerializer() {
		this.rankManager = Kingdoms.getInstance().getManager("rank", RankManager.class);
		this.handler = new OfflineKingdomPlayerHandler();
	}

	@Override
	public JsonElement serialize(OfflineKingdomPlayer player, Type type, JsonSerializationContext context) {
		JsonObject json = new JsonObject();
		json.addProperty("rank", player.getRank().getName());
		json.addProperty("uuid", player.getUniqueId() + "");
		return handler.serialize(player, json, context);
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
		return handler.deserialize(player, object, context);
	}

}
