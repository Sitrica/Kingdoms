package me.limeglass.kingdoms.database.serializers;

import java.lang.reflect.Type;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

import me.limeglass.kingdoms.database.Serializer;
import me.limeglass.kingdoms.database.handlers.LandHandler;
import me.limeglass.kingdoms.objects.kingdom.OfflineKingdom;
import me.limeglass.kingdoms.objects.land.Land;

public class LandSerializer implements Serializer<Land> {

	private final LandHandler handler;

	public LandSerializer() {
		this.handler = new LandHandler();
	}

	@Override
	public JsonElement serialize(Land land, Type type, JsonSerializationContext context) {
		JsonObject json = new JsonObject();
		json.addProperty("world", land.getWorld().getName());
		json.addProperty("claim-time", land.getClaimTime());
		json.addProperty("x", land.getX());
		json.addProperty("z", land.getZ());
		Optional<OfflineKingdom> kingdom = land.getKingdomOwner();
		if (kingdom.isPresent())
			json.addProperty("kingdom", kingdom.get().getName());
		return handler.serialize(land, json, context);
	}

	@Override
	public Land deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
		JsonElement worldElement = object.get("world");
		if (worldElement == null || worldElement.isJsonNull())
			return null;
		World world = Bukkit.getWorld(worldElement.getAsString());
		if (world == null)
			return null;
		int x = object.get("x").getAsInt();
		int z = object.get("z").getAsInt();
		Chunk chunk = world.getChunkAt(x, z);
		Land land = new Land(chunk);
		JsonElement claimElement = object.get("claim-time");
		if (claimElement != null && !claimElement.isJsonNull())
			land.setClaimTime(claimElement.getAsLong());
		JsonElement kingdomElement = object.get("kingdom");
		if (kingdomElement != null && !kingdomElement.isJsonNull())
			land.setKingdomOwner(kingdomElement.getAsString());
		return handler.deserialize(land, object, context);
	}

}
