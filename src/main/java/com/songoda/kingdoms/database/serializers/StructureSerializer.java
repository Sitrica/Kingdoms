package com.songoda.kingdoms.database.serializers;

import java.lang.reflect.Type;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.metadata.FixedMetadataValue;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.database.Serializer;
import com.songoda.kingdoms.manager.managers.KingdomManager;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.structures.Structure;
import com.songoda.kingdoms.objects.structures.StructureType;

public class StructureSerializer implements Serializer<Structure> {

	private final KingdomManager kingdomManager;
	private final Kingdoms instance;

	public StructureSerializer() {
		this.instance = Kingdoms.getInstance();
		this.kingdomManager = instance.getManager("kingdom", KingdomManager.class);
	}

	@Override
	public JsonElement serialize(Structure structure, Type type, JsonSerializationContext context) {
		JsonObject json = new JsonObject();
		Location location = structure.getLocation();
		json.addProperty("type", structure.getType().name());
		json.addProperty("world", location.getWorld().getName());
		json.addProperty("x", location.getX());
		json.addProperty("y", location.getY());
		json.addProperty("z", location.getZ());
		OfflineKingdom kingdom = structure.getKingdom();
		if (kingdom != null)
			json.addProperty("kingdom", kingdom.getName());
		return json;
	}

	@Override
	public Structure deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
		JsonElement kingdomElement = object.get("kingdom");
		if (kingdomElement == null || kingdomElement.isJsonNull())
			return null;
		Optional<OfflineKingdom> optional = kingdomManager.getOfflineKingdom(kingdomElement.getAsString());
		if (!optional.isPresent())
			return null;
		JsonElement worldElement = object.get("world");
		if (worldElement == null || worldElement.isJsonNull())
			return null;
		World world = Bukkit.getWorld(worldElement.getAsString());
		if (world == null)
			return null;
		double x = object.get("x").getAsDouble();
		double y = object.get("y").getAsDouble();
		double z = object.get("z").getAsDouble();
		Location location = new Location(world, x, y, z);
		JsonElement typeElement = object.get("type");
		if (typeElement == null || typeElement.isJsonNull())
			return null;
		StructureType structureType = StructureType.valueOf(typeElement.getAsString());
		if (structureType == null)
			return null;
		OfflineKingdom kingdom = optional.get();
		Structure structure = new Structure(kingdom, location, structureType);
		Block structureBlock = location.getBlock();
		structureBlock.setMetadata(structureType.getMetaData(), new FixedMetadataValue(instance, kingdom.getName()));
		return structure;
	}

}
