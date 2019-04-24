package com.songoda.kingdoms.database;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.songoda.kingdoms.database.serializers.DefenderInfoSerializer;
import com.songoda.kingdoms.database.serializers.KingdomChestSerializer;
import com.songoda.kingdoms.database.serializers.LandSerializer;
import com.songoda.kingdoms.database.serializers.LocationSerializer;
import com.songoda.kingdoms.database.serializers.MiscUpgradeSerializer;
import com.songoda.kingdoms.database.serializers.OfflineKingdomPlayerSerializer;
import com.songoda.kingdoms.database.serializers.PowerupSerializer;
import com.songoda.kingdoms.database.serializers.RankPermissionsSerializer;
import com.songoda.kingdoms.database.serializers.WarpPadSerializer;
import com.songoda.kingdoms.objects.kingdom.DefenderInfo;
import com.songoda.kingdoms.objects.kingdom.KingdomChest;
import com.songoda.kingdoms.objects.kingdom.MiscUpgrade;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.kingdom.Powerup;
import com.songoda.kingdoms.objects.kingdom.RankPermissions;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.player.OfflineKingdomPlayer;
import com.songoda.kingdoms.objects.structures.WarpPad;

import java.lang.reflect.Type;
import java.util.UUID;

import org.bukkit.Location;

public class OfflineKingdomSerializer implements Serializer<OfflineKingdom> {

	private final OfflineKingdomPlayerSerializer playerSerializer;
	private final RankPermissionsSerializer permissionsSerializer;
	private final MiscUpgradeSerializer miscUpgradeSerializer;
	private final DefenderInfoSerializer defenderSerializer;
	private final KingdomChestSerializer chestSerializer;
	private final LocationSerializer locationSerializer;
	private final PowerupSerializer powerupSerializer;
	private final WarpPadSerializer warpSerializer;
	private final LandSerializer landSerializer;

	// Anything that uses this class, must be casted through this constructor to avoid recurrent.
	OfflineKingdomSerializer() {
		this.playerSerializer = new OfflineKingdomPlayerSerializer(this);
		this.permissionsSerializer = new RankPermissionsSerializer();
		this.miscUpgradeSerializer = new MiscUpgradeSerializer(this);
		this.defenderSerializer = new DefenderInfoSerializer(this);
		this.chestSerializer = new KingdomChestSerializer(this);
		this.powerupSerializer = new PowerupSerializer(this);
		this.locationSerializer = new LocationSerializer();
		this.warpSerializer = new WarpPadSerializer(this);
		this.landSerializer = new LandSerializer(this);
	}

	@Override
	public JsonElement serialize(OfflineKingdom kingdom, Type type, JsonSerializationContext context) {
		JsonObject json = new JsonObject();
		json.addProperty("lore", kingdom.getLore());
		json.addProperty("name", kingdom.getName());
		json.addProperty("neutral", kingdom.isNeutral());
		json.addProperty("invaded", kingdom.hasInvaded());
		json.addProperty("uuid", kingdom.getUniqueId() + "");
		json.addProperty("max-members", kingdom.getMaxMembers());
		json.addProperty("first-claim", kingdom.hasUsedFirstClaim());
		json.addProperty("resource-points", kingdom.getResourcePoints());
		json.addProperty("invasion-cooldown", kingdom.getInvasionCooldown());
		json.add("spawn", locationSerializer.serialize(kingdom.getSpawn(), Location.class, context));
		json.add("powerup", powerupSerializer.serialize(kingdom.getPowerup(), Powerup.class, context));
		json.add("king", playerSerializer.serialize(kingdom.getKing(), OfflineKingdomPlayer.class, context));
		json.add("chest", chestSerializer.serialize(kingdom.getKingdomChest(), KingdomChest.class, context));
		json.add("nexus", locationSerializer.serialize(kingdom.getNexusLocation(), Location.class, context));
		json.add("defender-info", defenderSerializer.serialize(kingdom.getDefenderInfo(), DefenderInfo.class, context));
		json.add("misc-upgrades", miscUpgradeSerializer.serialize(kingdom.getMiscUpgrades(), MiscUpgrade.class, context));
		JsonArray claims = new JsonArray();
		kingdom.getClaims().forEach(land -> claims.add(landSerializer.serialize(land, Land.class, context)));
		json.add("claims", claims);
		JsonArray warps = new JsonArray();
		kingdom.getWarps().forEach(warp -> warps.add(warpSerializer.serialize(warp, WarpPad.class, context)));
		json.add("warps", warps);
		JsonArray allies = new JsonArray();
		kingdom.getAllies().forEach(ally -> allies.add(serialize(ally, OfflineKingdom.class, context)));
		json.add("allies", allies);
		JsonArray eneimies = new JsonArray();
		kingdom.getEnemies().forEach(enemy -> eneimies.add(serialize(enemy, OfflineKingdom.class, context)));
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
	public OfflineKingdom deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
		JsonElement uuidElement = object.get("uuid");
		if (uuidElement == null || uuidElement.isJsonNull())
			return null;
		UUID uuid = UUID.fromString(uuidElement.getAsString());
		if (uuid == null)
			return null;
		JsonElement kingElement = object.get("king");
		if (kingElement == null || kingElement.isJsonNull())
			return null;
		OfflineKingdomPlayer king = playerSerializer.deserialize(kingElement, OfflineKingdomPlayer.class, context);
		if (king == null)
			return null;
		OfflineKingdom kingdom = new OfflineKingdom(uuid, king);
		JsonElement powerupElement = object.get("powerup");
		if (powerupElement != null && !powerupElement.isJsonNull()) {
			Powerup powerup = powerupSerializer.deserialize(powerupElement, Powerup.class, context);
			if (powerup != null)
				kingdom.setPowerup(powerup);
		}
		JsonElement nameElement = object.get("name");
		if (nameElement != null && !nameElement.isJsonNull())
			kingdom.setName(nameElement.getAsString());
		JsonElement loreElement = object.get("lore");
		if (loreElement != null && !loreElement.isJsonNull())
			kingdom.setLore(loreElement.getAsString());
		JsonElement spawnElement = object.get("spawn");
		if (spawnElement != null && !spawnElement.isJsonNull())
			kingdom.setSpawn(locationSerializer.deserialize(spawnElement, Location.class, context));
		JsonElement nexusElement = object.get("nexus");
		if (nexusElement != null && !nexusElement.isJsonNull())
			kingdom.setNexusLocation(locationSerializer.deserialize(nexusElement, Location.class, context));
		JsonElement miscUpgradesElement = object.get("misc-upgrades");
		if (miscUpgradesElement != null && !miscUpgradesElement.isJsonNull())
			kingdom.setMiscUpgrades(miscUpgradeSerializer.deserialize(miscUpgradesElement, MiscUpgrade.class, context));
		JsonElement defenderElement = object.get("defender-info");
		if (defenderElement != null && !defenderElement.isJsonNull())
			kingdom.setDefenderInfo(defenderSerializer.deserialize(defenderElement, DefenderInfo.class, context));
		JsonElement chestElement = object.get("chest");
		if (chestElement != null && !chestElement.isJsonNull())
			kingdom.setKingdomChest(chestSerializer.deserialize(chestElement, KingdomChest.class, context));
		JsonElement neutralElement = object.get("neutral");
		if (neutralElement != null && !neutralElement.isJsonNull())
			kingdom.setNeutral(neutralElement.getAsBoolean());
		JsonElement firstElement = object.get("first-claim");
		if (firstElement != null && !firstElement.isJsonNull())
			kingdom.setUsedFirstClaim(firstElement.getAsBoolean());
		JsonElement invadedElement = object.get("invaded");
		if (invadedElement != null && !invadedElement.isJsonNull())
			kingdom.setInvaded(invadedElement.getAsBoolean());
		JsonElement pointsElement = object.get("resource-points");
		if (pointsElement != null && !pointsElement.isJsonNull())
			kingdom.setResourcePoints(pointsElement.getAsLong());
		JsonElement invasionElement = object.get("invasion-cooldown");
		if (invasionElement != null && !invasionElement.isJsonNull())
			kingdom.setInvasionCooldown(invasionElement.getAsLong());
		JsonElement maxElement = object.get("max-members");
		if (maxElement != null && !maxElement.isJsonNull())
			kingdom.setMaxMembers(maxElement.getAsInt());
		JsonElement claimsElement = object.get("claims");
		if (claimsElement != null && !claimsElement.isJsonNull() && claimsElement.isJsonArray()) {
			JsonArray array = claimsElement.getAsJsonArray();
			array.forEach(element -> {
				Land land = landSerializer.deserialize(element, Land.class, context);
				if (land != null)
					kingdom.addClaim(land);
			});
		}
		JsonElement warpsElement = object.get("warps");
		if (warpsElement != null && !warpsElement.isJsonNull() && warpsElement.isJsonArray()) {
			JsonArray array = warpsElement.getAsJsonArray();
			array.forEach(element -> {
				WarpPad warp = warpSerializer.deserialize(element, WarpPad.class, context);
				if (warp != null)
					kingdom.addWarp(warp);
			});
		}
		JsonElement alliesElement = object.get("allies");
		if (alliesElement != null && !alliesElement.isJsonNull() && alliesElement.isJsonArray()) {
			JsonArray array = alliesElement.getAsJsonArray();
			array.forEach(element -> {
				OfflineKingdom ally = deserialize(element, OfflineKingdom.class, context);
				if (ally != null)
					kingdom.addAlliance(ally);
			});
		}
		JsonElement eneimiesElement = object.get("eneimies");
		if (eneimiesElement != null && !eneimiesElement.isJsonNull() && eneimiesElement.isJsonArray()) {
			JsonArray array = eneimiesElement.getAsJsonArray();
			array.forEach(element -> {
				OfflineKingdom enemy = deserialize(element, OfflineKingdom.class, context);
				if (enemy != null)
					kingdom.addEnemy(enemy);
			});
		}
		JsonElement membersElement = object.get("members");
		if (membersElement != null && !membersElement.isJsonNull() && membersElement.isJsonArray()) {
			JsonArray array = membersElement.getAsJsonArray();
			array.forEach(element -> {
				OfflineKingdomPlayer member = playerSerializer.deserialize(element, OfflineKingdomPlayer.class, context);
				if (member != null)
					kingdom.addMember(member);
			});
		}
		JsonElement permissionsElement = object.get("permissions");
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
