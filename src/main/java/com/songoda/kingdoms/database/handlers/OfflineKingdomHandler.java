package com.songoda.kingdoms.database.handlers;

import org.bukkit.Location;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.songoda.kingdoms.database.serializers.DefenderInfoSerializer;
import com.songoda.kingdoms.database.serializers.LandSerializer;
import com.songoda.kingdoms.database.serializers.LocationSerializer;
import com.songoda.kingdoms.database.serializers.OfflineKingdomPlayerSerializer;
import com.songoda.kingdoms.database.serializers.PowerupSerializer;
import com.songoda.kingdoms.database.serializers.RankPermissionsSerializer;
import com.songoda.kingdoms.database.serializers.WarpPadSerializer;
import com.songoda.kingdoms.objects.kingdom.DefenderInfo;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.kingdom.Powerup;
import com.songoda.kingdoms.objects.kingdom.RankPermissions;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.player.OfflineKingdomPlayer;
import com.songoda.kingdoms.objects.structures.WarpPad;

public class OfflineKingdomHandler implements Handler<OfflineKingdom> {

	private final OfflineKingdomPlayerSerializer playerSerializer;
	private final RankPermissionsSerializer permissionsSerializer;
	private final DefenderInfoSerializer defenderSerializer;
	private final LocationSerializer locationSerializer;
	private final PowerupSerializer powerupSerializer;
	private final WarpPadSerializer warpSerializer;
	private final LandSerializer landSerializer;

	public OfflineKingdomHandler() {
		this.playerSerializer = new OfflineKingdomPlayerSerializer();
		this.permissionsSerializer = new RankPermissionsSerializer();
		this.defenderSerializer = new DefenderInfoSerializer();
		this.locationSerializer = new LocationSerializer();
		this.powerupSerializer = new PowerupSerializer();
		this.warpSerializer = new WarpPadSerializer();
		this.landSerializer = new LandSerializer();
	}

	@Override
	public JsonObject serialize(OfflineKingdom kingdom, JsonObject json, JsonSerializationContext context) {
		json.add("spawn", locationSerializer.serialize(kingdom.getSpawn(), Location.class, context));
		json.add("powerup", powerupSerializer.serialize(kingdom.getPowerup(), Powerup.class, context));
		json.add("king", playerSerializer.serialize(kingdom.getOwner(), OfflineKingdomPlayer.class, context));
		json.add("nexus", locationSerializer.serialize(kingdom.getNexusLocation(), Location.class, context));
		json.add("defender-info", defenderSerializer.serialize(kingdom.getDefenderInfo(), DefenderInfo.class, context));
		JsonArray claims = new JsonArray();
		kingdom.getClaims().forEach(land -> claims.add(landSerializer.serialize(land, Land.class, context)));
		json.add("claims", claims);
		JsonArray warps = new JsonArray();
		kingdom.getWarps().forEach(warp -> warps.add(warpSerializer.serialize(warp, WarpPad.class, context)));
		json.add("warps", warps);
		JsonArray allies = new JsonArray();
		kingdom.getAllies().forEach(ally -> allies.add(ally.getName()));
		json.add("allies", allies);
		JsonArray eneimies = new JsonArray();
		kingdom.getEnemies().forEach(enemy -> eneimies.add(enemy.getName()));
		json.add("eneimies", eneimies);
		JsonArray members = new JsonArray();
		kingdom.getMembers().forEach(member -> members.add(playerSerializer.serialize(member, OfflineKingdomPlayer.class, context)));
		json.add("members", members);
		JsonArray permissions = new JsonArray();
		kingdom.getPermissions().forEach(permission -> permissions.add(permissionsSerializer.serialize(permission, RankPermissions.class, context)));
		json.add("permissions", permissions);
		return json;
	}

	@Override
	public OfflineKingdom deserialize(OfflineKingdom kingdom, JsonObject json, JsonDeserializationContext context) {
		JsonElement spawnElement = json.get("spawn");
		if (spawnElement != null && !spawnElement.isJsonNull())
			kingdom.setSpawn(locationSerializer.deserialize(spawnElement, Location.class, context));
		JsonElement nexusElement = json.get("nexus");
		if (nexusElement != null && !nexusElement.isJsonNull())
			kingdom.setNexusLocation(locationSerializer.deserialize(nexusElement, Location.class, context));
		JsonElement claimsElement = json.get("claims");
		if (claimsElement != null && !claimsElement.isJsonNull() && claimsElement.isJsonArray()) {
			JsonArray array = claimsElement.getAsJsonArray();
			array.forEach(element -> {
				Land land = landSerializer.deserialize(element, Land.class, context);
				if (land != null)
					kingdom.addClaim(land);
			});
		}
		JsonElement warpsElement = json.get("warps");
		if (warpsElement != null && !warpsElement.isJsonNull() && warpsElement.isJsonArray()) {
			JsonArray array = warpsElement.getAsJsonArray();
			array.forEach(element -> {
				WarpPad warp = warpSerializer.deserialize(element, WarpPad.class, context);
				if (warp != null)
					kingdom.addWarp(warp);
			});
		}
		JsonElement alliesElement = json.get("allies");
		if (alliesElement != null && !alliesElement.isJsonNull() && alliesElement.isJsonArray()) {
			JsonArray array = alliesElement.getAsJsonArray();
			array.forEach(element -> kingdom.addAlliance(element.getAsString()));
		}
		JsonElement eneimiesElement = json.get("eneimies");
		if (eneimiesElement != null && !eneimiesElement.isJsonNull() && eneimiesElement.isJsonArray()) {
			JsonArray array = eneimiesElement.getAsJsonArray();
			array.forEach(element -> kingdom.addEnemy(element.getAsString()));
		}
		JsonElement membersElement = json.get("members");
		if (membersElement != null && !membersElement.isJsonNull() && membersElement.isJsonArray()) {
			JsonArray array = membersElement.getAsJsonArray();
			array.forEach(element -> {
				OfflineKingdomPlayer member = playerSerializer.deserialize(element, OfflineKingdomPlayer.class, context);
				if (member != null)
					kingdom.addMember(member);
			});
		}
		JsonElement permissionsElement = json.get("permissions");
		if (permissionsElement != null && !permissionsElement.isJsonNull() && permissionsElement.isJsonArray()) {
			JsonArray array = permissionsElement.getAsJsonArray();
			array.forEach(element -> {
				RankPermissions permissions = permissionsSerializer.deserialize(element, RankPermissions.class, context);
				if (permissions != null)
					kingdom.setRankPermissions(permissions);
			});
		}
		return kingdom;
	}

}
