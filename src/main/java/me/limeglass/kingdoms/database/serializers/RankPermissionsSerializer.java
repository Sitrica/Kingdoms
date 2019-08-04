package me.limeglass.kingdoms.database.serializers;

import java.lang.reflect.Type;
import java.util.Optional;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

import me.limeglass.kingdoms.Kingdoms;
import me.limeglass.kingdoms.database.Serializer;
import me.limeglass.kingdoms.manager.managers.RankManager;
import me.limeglass.kingdoms.manager.managers.RankManager.Rank;
import me.limeglass.kingdoms.objects.kingdom.RankPermissions;

public class RankPermissionsSerializer implements Serializer<RankPermissions> {

	@Override
	public JsonElement serialize(RankPermissions permissions, Type type, JsonSerializationContext context) {
		JsonObject json = new JsonObject();
		json.addProperty("kick", permissions.canKick());
		json.addProperty("enemy", permissions.canEnemy());
		json.addProperty("claim", permissions.canClaim());
		json.addProperty("lore", permissions.canSetLore());
		json.addProperty("unclaim", permissions.canUnclaim());
		json.addProperty("can-build", permissions.canBuild());
		json.addProperty("can-invade", permissions.canInvade());
		json.addProperty("can-invite", permissions.canInvite());
		json.addProperty("set-spawn", permissions.canSetSpawn());
		json.addProperty("use-spawn", permissions.canUseSpawn());
		json.addProperty("broadcast", permissions.canBroadcast());
		json.addProperty("rank", permissions.getRank().getName());
		json.addProperty("can-alliance", permissions.canAlliance());
		json.addProperty("use-turrets", permissions.canUseTurrets());
		json.addProperty("max-claims", permissions.getMaximumClaims());
		json.addProperty("chest-access", permissions.hasChestAccess());
		json.addProperty("nexus-access", permissions.hasNexusAccess());
		json.addProperty("build-in-nexus", permissions.canBuildInNexus());
		json.addProperty("grab-experience", permissions.canGrabExperience());
		json.addProperty("build-structures", permissions.canBuildStructures());
		json.addProperty("access-protected", permissions.canAccessProtected());
		json.addProperty("edit-permissions", permissions.canEditPermissions());
		return json;
	}

	@Override
	public RankPermissions deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
		RankManager rankManager = Kingdoms.getInstance().getManager(RankManager.class);
		JsonObject object = json.getAsJsonObject();
		Rank rank = Optional.ofNullable(object.get("rank"))
				.filter(element -> !element.isJsonNull())
				.map(element -> element.getAsString())
				.map(name -> rankManager.getRank(name))
				.filter(found -> found.isPresent())
				.map(found -> found.get())
				.orElse(rankManager.getDefaultRank());
		RankPermissions permissions = new RankPermissions(rank);
		JsonElement protectedElement = object.get("access-protected");
		if (protectedElement == null || protectedElement.isJsonNull())
			return permissions;
		permissions.setProtectedAccess(protectedElement.getAsBoolean());
		JsonElement permissionsElement = object.get("edit-permissions");
		if (permissionsElement != null && !permissionsElement.isJsonNull())
			permissions.setEditPermissions(permissionsElement.getAsBoolean());
		JsonElement kickElement = object.get("kick");
		if (kickElement != null && !kickElement.isJsonNull())
			permissions.setKick(kickElement.getAsBoolean());
		JsonElement enemyElement = object.get("enemy");
		if (enemyElement != null && !enemyElement.isJsonNull())
			permissions.setEnemy(enemyElement.getAsBoolean());
		JsonElement structuresElement = object.get("build-structures");
		if (structuresElement != null && !structuresElement.isJsonNull())
			permissions.setBuildStructures(structuresElement.getAsBoolean());
		JsonElement nexusBuildElement = object.get("build-in-nexus");
		if (nexusBuildElement != null && !nexusBuildElement.isJsonNull())
			permissions.setNexusBuild(nexusBuildElement.getAsBoolean());
		JsonElement unclaimElement = object.get("unclaim");
		if (unclaimElement != null && !unclaimElement.isJsonNull())
			permissions.setUnclaiming(unclaimElement.getAsBoolean());
		JsonElement broadcastElement = object.get("broadcast");
		if (broadcastElement != null && !broadcastElement.isJsonNull())
			permissions.setBroadcast(broadcastElement.getAsBoolean());
		JsonElement nexusElement = object.get("nexus-access");
		if (nexusElement != null && !nexusElement.isJsonNull())
			permissions.setNexusAccess(nexusElement.getAsBoolean());
		JsonElement claimElement = object.get("claim");
		if (claimElement != null && !claimElement.isJsonNull())
			permissions.setClaiming(claimElement.getAsBoolean());
		JsonElement chestElement = object.get("chest-access");
		if (chestElement != null && !chestElement.isJsonNull())
			permissions.setChestAccess(chestElement.getAsBoolean());
		JsonElement experienceElement = object.get("grab-experience");
		if (experienceElement != null && !experienceElement.isJsonNull())
			permissions.setGrabExperience(experienceElement.getAsBoolean());
		JsonElement useSpawnElement = object.get("use-spawn");
		if (useSpawnElement != null && !useSpawnElement.isJsonNull())
			permissions.setUseSpawn(useSpawnElement.getAsBoolean());
		JsonElement allianceElement = object.get("can-alliance");
		if (allianceElement != null && !allianceElement.isJsonNull())
			permissions.setAlliance(allianceElement.getAsBoolean());
		JsonElement turretsElement = object.get("use-turrets");
		if (turretsElement != null && !turretsElement.isJsonNull())
			permissions.setTurrets(turretsElement.getAsBoolean());
		JsonElement inviteElement = object.get("can-invite");
		if (inviteElement != null && !inviteElement.isJsonNull())
			permissions.setInvite(inviteElement.getAsBoolean());
		JsonElement claimsElement = object.get("max-claims");
		if (claimsElement != null && !claimsElement.isJsonNull())
			permissions.setMaximumClaims(claimsElement.getAsInt());
		JsonElement invadeElement = object.get("can-invade");
		if (invadeElement != null && !invadeElement.isJsonNull())
			permissions.setInvade(invadeElement.getAsBoolean());
		JsonElement setSpawnElement = object.get("set-spawn");
		if (setSpawnElement != null && !setSpawnElement.isJsonNull())
			permissions.setSpawn(setSpawnElement.getAsBoolean());
		JsonElement buildElement = object.get("can-build");
		if (buildElement != null && !buildElement.isJsonNull())
			permissions.setBuild(buildElement.getAsBoolean());
		JsonElement loreElement = object.get("lore");
		if (loreElement != null && !loreElement.isJsonNull())
			permissions.setLore(loreElement.getAsBoolean());
		return permissions;
	}

}
