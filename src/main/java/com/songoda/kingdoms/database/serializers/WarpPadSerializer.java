package com.songoda.kingdoms.database.serializers;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.songoda.kingdoms.database.OfflineKingdomSerializer;
import com.songoda.kingdoms.database.Serializer;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.structures.Structure;
import com.songoda.kingdoms.objects.structures.WarpPad;

public class WarpPadSerializer implements Serializer<WarpPad> {

	private final StructureSerializer structureSerializer;
	private final LandSerializer landSerializer;

	public WarpPadSerializer(OfflineKingdomSerializer kingdomSerializer) {
		this.structureSerializer = new StructureSerializer(kingdomSerializer);
		this.landSerializer = new LandSerializer(kingdomSerializer);
	}

	@Override
	public JsonElement serialize(WarpPad warp, Type type, JsonSerializationContext context) {
		JsonObject json = new JsonObject();
		json.addProperty("name", warp.getName());
		json.add("structure", structureSerializer.serialize(warp, Structure.class, context));
		json.add("land", landSerializer.serialize(warp.getLand(), Land.class, context));
		return json;
	}

	@Override
	public WarpPad deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
		JsonElement nameElement = object.get("name");
		if (nameElement == null || nameElement.isJsonNull())
			return null;
		String name = nameElement.getAsString();
		JsonElement structureElement = object.get("structure");
		if (structureElement == null || structureElement.isJsonNull() || !structureElement.isJsonObject())
			return null;
		Structure structure = structureSerializer.deserialize(structureElement, Structure.class, context);
		JsonElement landElement = object.get("land");
		if (landElement == null || landElement.isJsonNull())
			return null;
		Land land = landSerializer.deserialize(landElement, Land.class, context);
		if (land == null || structure == null)
			return null;
		return new WarpPad(structure.getKingdom(), structure.getLocation(), name, land);
	}

}
