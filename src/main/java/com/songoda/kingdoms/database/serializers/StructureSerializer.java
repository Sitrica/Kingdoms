package com.songoda.kingdoms.database.serializers;

import java.lang.reflect.Type;

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
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.structures.Extractor;
import com.songoda.kingdoms.objects.structures.Structure;
import com.songoda.kingdoms.objects.structures.StructureType;

public class StructureSerializer implements Serializer<Structure> {

	private final Kingdoms instance;

	public StructureSerializer() {
		this.instance = Kingdoms.getInstance();
	}

	@Override
	public JsonElement serialize(Structure structure, Type type, JsonSerializationContext context) {
		JsonObject json = new JsonObject();
		Location location = structure.getLocation();
		StructureType strucutreType = structure.getType();
		json.addProperty("type", strucutreType.name());
		json.addProperty("world", location.getWorld().getName());
		json.addProperty("x", location.getX());
		json.addProperty("y", location.getY());
		json.addProperty("z", location.getZ());
		OfflineKingdom kingdom = structure.getKingdom();
		if (kingdom != null)
			json.addProperty("kingdom", kingdom.getName());
		switch (strucutreType) {
			case ARSENAL:
				break;
			case EXTRACTOR:
				Extractor extractor = (Extractor) structure;
				json.addProperty("last", extractor.getLastReset());
				break;
			case NEXUS:
				break;
			case OUTPOST:
				break;
			case POWERCELL:
				break;
			case RADAR:
				break;
			case SIEGE_ENGINE:
				break;
			case WARPPAD:
				break;
		}
		return json;
	}

	@Override
	public Structure deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
		JsonElement kingdomElement = object.get("kingdom");
		if (kingdomElement == null || kingdomElement.isJsonNull())
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
		String kingdom = kingdomElement.getAsString();
		Structure structure = new Structure(kingdom, location, structureType);
		switch (structureType) {
			case ARSENAL:
				break;
			case EXTRACTOR:
				JsonElement lastElement = object.get("last");
				if (lastElement == null || lastElement.isJsonNull())
					return null;
				structure = new Extractor(kingdom, location, lastElement.getAsLong());
				break;
			case NEXUS:
				break;
			case OUTPOST:
				break;
			case POWERCELL:
				break;
			case RADAR:
				break;
			case SIEGE_ENGINE:
				break;
			case WARPPAD:
				break;
		}
		Block structureBlock = location.getBlock();
		structureBlock.setMetadata(structureType.getMetaData(), new FixedMetadataValue(instance, kingdom));
		return structure;
	}

}
