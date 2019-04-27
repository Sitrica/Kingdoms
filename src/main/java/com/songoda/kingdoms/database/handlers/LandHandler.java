package com.songoda.kingdoms.database.handlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.songoda.kingdoms.database.serializers.StructureSerializer;
import com.songoda.kingdoms.database.serializers.TurretSerializer;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.structures.Structure;
import com.songoda.kingdoms.objects.turrets.Turret;

public class LandHandler implements Handler<Land> {

	private final StructureSerializer structureSerializer;
	private final TurretSerializer turretSerializer;

	public LandHandler() {
		this.structureSerializer = new StructureSerializer();
		this.turretSerializer = new TurretSerializer();
	}

	@Override
	public JsonObject serialize(Land land, JsonObject json, JsonSerializationContext context) {
		json.add("structure", structureSerializer.serialize(land.getStructure(), Structure.class, context));
		JsonArray turrets = new JsonArray();
		land.getTurrets().forEach(turret -> turrets.add(turretSerializer.serialize(turret, Turret.class, context)));
		json.add("turrets", turrets);
		return json;
	}

	@Override
	public Land deserialize(Land land, JsonObject json, JsonDeserializationContext context) {
		JsonElement structureElement = json.get("structure");
		if (structureElement != null && !structureElement.isJsonNull())
			land.setStructure(structureSerializer.deserialize(structureElement, Structure.class, context));
		JsonElement turretElement = json.get("turrets");
		if (turretElement != null && !turretElement.isJsonNull() && turretElement.isJsonArray()) {
			JsonArray array = turretElement.getAsJsonArray();
			array.forEach(element -> {
				Turret turret = turretSerializer.deserialize(element, Turret.class, context);
				if (turret != null)
					land.addTurret(turret);
			});
		}
		return land;
	}

}
