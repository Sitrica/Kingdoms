package com.songoda.kingdoms.database.serializers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.database.Serializer;
import com.songoda.kingdoms.manager.managers.TurretManager;
import com.songoda.kingdoms.objects.turrets.Turret;
import com.songoda.kingdoms.objects.turrets.TurretType;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.lang.reflect.Type;
import java.util.Optional;

public class TurretSerializer implements Serializer<Turret> {

	private final TurretManager turretManager;

	public TurretSerializer() {
		this.turretManager = Kingdoms.getInstance().getManager("turret", TurretManager.class);
	}

	@Override
	public JsonElement serialize(Turret turret, Type type, JsonSerializationContext context) {
		JsonObject json = new JsonObject();
		Location location = turret.getLocation();
		json.addProperty("post", turret.hasCreatedPost());
		json.addProperty("type", turret.getType().getName());
		json.addProperty("world", location.getWorld().getName());
		json.addProperty("x", location.getX());
		json.addProperty("y", location.getY());
		json.addProperty("z", location.getZ());
		return json;
	}

	@Override
	public Turret deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
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
		Optional<TurretType> turretType = turretManager.getTurretTypeByName(typeElement.getAsString());
		if (!turretType.isPresent())
			return null;
		JsonElement postElement = object.get("post");
		if (postElement == null || postElement.isJsonNull())
			return null;
		return new Turret(location, turretType.get(), postElement.getAsBoolean());
	}

}
