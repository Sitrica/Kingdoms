package com.songoda.kingdoms.database.handlers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.songoda.kingdoms.database.serializers.LandSerializer;
import com.songoda.kingdoms.database.serializers.StructureSerializer;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.structures.Structure;
import com.songoda.kingdoms.objects.structures.WarpPad;

public class WarpPadHandler implements Handler<WarpPad> {

	private final StructureSerializer structureSerializer;
	private final LandSerializer landSerializer;

	public WarpPadHandler() {
		this.structureSerializer = new StructureSerializer();
		this.landSerializer = new LandSerializer();
	}

	@Override
	public JsonObject serialize(WarpPad warp, JsonObject json, JsonSerializationContext context) {
		json.add("structure", structureSerializer.serialize(warp, Structure.class, context));
		json.add("land", landSerializer.serialize(warp.getLand(), Land.class, context));
		return json;
	}

	@Override
	public WarpPad deserialize(WarpPad warp, JsonObject json, JsonDeserializationContext context) {
		JsonElement nameElement = json.get("name");
		if (nameElement == null || nameElement.isJsonNull())
			return null;
		String name = nameElement.getAsString();
		JsonElement structureElement = json.get("structure");
		if (structureElement == null || structureElement.isJsonNull() || !structureElement.isJsonObject())
			return null;
		Structure structure = structureSerializer.deserialize(structureElement, Structure.class, context);
		JsonElement landElement = json.get("land");
		if (landElement == null || landElement.isJsonNull())
			return null;
		Land land = landSerializer.deserialize(landElement, Land.class, context);
		if (land == null || structure == null)
			return null;
		return new WarpPad(structure.getKingdom(), structure.getLocation(), name, land);
	}

}
