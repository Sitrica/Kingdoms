package me.limeglass.kingdoms.database.serializers;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

import me.limeglass.kingdoms.database.Serializer;
import me.limeglass.kingdoms.objects.land.Land;
import me.limeglass.kingdoms.objects.structures.Structure;
import me.limeglass.kingdoms.objects.structures.WarpPad;

public class WarpPadSerializer implements Serializer<WarpPad> {

	private final StructureSerializer structureSerializer;
	private final LandSerializer landSerializer;

	public WarpPadSerializer() {
		this.structureSerializer = new StructureSerializer();
		this.landSerializer = new LandSerializer();
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
		return new WarpPad(structure.getKingdom().getName(), structure.getLocation(), name, land);
	}

}
