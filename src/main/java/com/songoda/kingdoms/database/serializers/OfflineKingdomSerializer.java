package com.songoda.kingdoms.database.serializers;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.inventory.ItemStack;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.songoda.kingdoms.database.Serializer;
import com.songoda.kingdoms.database.handlers.OfflineKingdomHandler;
import com.songoda.kingdoms.objects.kingdom.DefenderInfo;
import com.songoda.kingdoms.objects.kingdom.DefenderUpgrade;
import com.songoda.kingdoms.objects.kingdom.KingdomChest;
import com.songoda.kingdoms.objects.kingdom.MiscUpgrade;
import com.songoda.kingdoms.objects.kingdom.MiscUpgradeType;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.kingdom.Powerup;
import com.songoda.kingdoms.objects.kingdom.PowerupType;
import com.songoda.kingdoms.utils.Utils;

public class OfflineKingdomSerializer implements Serializer<OfflineKingdom> {

	private final ItemStackSerializer itemSerializer;
	private final OfflineKingdomHandler handler;

	public OfflineKingdomSerializer() {
		this.itemSerializer = new ItemStackSerializer();
		this.handler = new OfflineKingdomHandler();
	}

	@Override
	public JsonElement serialize(OfflineKingdom kingdom, Type type, JsonSerializationContext context) {
		JsonObject json = new JsonObject();
		json.addProperty("lore", kingdom.getLore());
		json.addProperty("name", kingdom.getName());
		json.addProperty("neutral", kingdom.isNeutral());
		json.addProperty("invaded", kingdom.hasInvaded());
		json.addProperty("max-members", kingdom.getMaxMembers());
		json.addProperty("first-claim", kingdom.hasUsedFirstClaim());
		json.addProperty("owner", kingdom.getOwner().getUniqueId() + "");
		json.addProperty("resource-points", kingdom.getResourcePoints());
		json.addProperty("extra-purchased", kingdom.getExtraPurchased());
		json.addProperty("invasion-cooldown", kingdom.getInvasionCooldown());
		JsonObject chest = new JsonObject();
		JsonObject contentsObject = new JsonObject();
		Map<Integer, ItemStack> contents = kingdom.getKingdomChest().getContents();
		if (contents != null && !contents.isEmpty()) {
			for (Entry<Integer, ItemStack> entry : contents.entrySet()) {
				contentsObject.add(entry.getKey() + "", itemSerializer.serialize(entry.getValue(), ItemStack.class, context));
			}
		}
		chest.add("contents", contentsObject);
		json.add("chest", chest);
		JsonObject miscUpgrade = new JsonObject();
		JsonObject bought = new JsonObject();
		MiscUpgrade upgrade = kingdom.getMiscUpgrades();
		for (MiscUpgradeType upgradeType : MiscUpgradeType.values())
			bought.addProperty(upgradeType.name(), upgrade.hasBought(upgradeType));
		miscUpgrade.add("bought", bought);
		JsonObject enabled = new JsonObject();
		for (MiscUpgradeType upgradeType : MiscUpgradeType.values())
			enabled.addProperty(upgradeType.name(), upgrade.isEnabled(upgradeType));
		miscUpgrade.add("enabled", enabled);
		json.add("misc-upgrades", miscUpgrade);
		Powerup powerup = kingdom.getPowerup();
		JsonObject powerups = new JsonObject();
		for (PowerupType powerupType : PowerupType.values())
			powerups.addProperty(powerupType.name(), powerup.getLevel(powerupType));
		json.add("powerups", powerups);
		return handler.serialize(kingdom, json, context);
	}

	@Override
	public OfflineKingdom deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
		JsonElement ownerElement = object.get("owner");
		if (ownerElement == null || ownerElement.isJsonNull())
			return null;
		UUID uuid = Utils.getUniqueId(ownerElement.getAsString());
		if (uuid == null)
			return null;
		JsonElement nameElement = object.get("name");
		if (nameElement == null || nameElement.isJsonNull())
			return null;
		OfflineKingdom kingdom = new OfflineKingdom(uuid, nameElement.getAsString());
		JsonElement loreElement = object.get("lore");
		if (loreElement != null && !loreElement.isJsonNull())
			kingdom.setLore(loreElement.getAsString());
		JsonElement neutralElement = object.get("neutral");
		if (neutralElement != null && !neutralElement.isJsonNull())
			kingdom.setNeutral(neutralElement.getAsBoolean());
		JsonElement extraElement = object.get("extra-purchased");
		if (extraElement != null && !extraElement.isJsonNull())
			kingdom.setExtraPurchased(extraElement.getAsInt());
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
		Powerup powerup = kingdom.getPowerup();
		JsonElement powerupsElement = object.get("powerups");
		if (powerupsElement != null && !powerupsElement.isJsonNull() && powerupsElement.isJsonObject()) {
			JsonObject powerupsObject = powerupsElement.getAsJsonObject();
			for (PowerupType powerupType : PowerupType.values()) {
				JsonElement element = powerupsObject.get(powerupType.name());
				if (element == null || element.isJsonNull())
					continue;
				powerup.setLevel(element.getAsInt(), powerupType);
			}
		}
		JsonElement miscUpgradesElement = object.get("misc-upgrades");
		if (miscUpgradesElement != null && !miscUpgradesElement.isJsonNull() && miscUpgradesElement.isJsonObject()) {
			JsonObject miscUpgradesObject = miscUpgradesElement.getAsJsonObject();
			MiscUpgrade upgrade = new MiscUpgrade(kingdom);
			JsonElement boughtElement = miscUpgradesObject.get("bought");
			if (boughtElement != null && !boughtElement.isJsonNull() && boughtElement.isJsonObject()) {
				JsonObject boughtObject = boughtElement.getAsJsonObject();
				for (MiscUpgradeType upgradeType : MiscUpgradeType.values()) {
					JsonElement element = boughtObject.get(upgradeType.name());
					if (element == null || element.isJsonNull())
						continue;
					upgrade.setBought(upgradeType, element.getAsBoolean());
				}
			}
			JsonElement enabledElement = object.get("enabled");
			if (enabledElement != null && !enabledElement.isJsonNull() && enabledElement.isJsonObject()) {
				JsonObject enabledObject = enabledElement.getAsJsonObject();
				for (MiscUpgradeType upgradeType : MiscUpgradeType.values()) {
					JsonElement element = enabledObject.get(upgradeType.name());
					if (element == null || element.isJsonNull())
						continue;
					upgrade.setEnabled(upgradeType, element.getAsBoolean());
				}
			}
			kingdom.setMiscUpgrades(upgrade);
		}
		JsonElement defenderElement = object.get("defender-info");
		if (defenderElement != null && !defenderElement.isJsonNull() && defenderElement.isJsonObject()) {
			JsonObject defenderObject = defenderElement.getAsJsonObject();
			DefenderInfo info = new DefenderInfo(kingdom);
			JsonElement upgradesElement = defenderObject.get("upgrades");
			if (upgradesElement != null && !upgradesElement.isJsonNull() && upgradesElement.isJsonObject()) {
				JsonObject upgradesObject = upgradesElement.getAsJsonObject();
				for (DefenderUpgrade upgradeType : DefenderUpgrade.values()) {
					JsonElement element = upgradesObject.get(upgradeType.name());
					if (element == null || element.isJsonNull())
						continue;
					info.setUpgradeLevel(upgradeType, element.getAsInt());
				}
			}
			kingdom.setDefenderInfo(info);
		}
		JsonElement chestElement = object.get("chest");
		if (chestElement != null && !chestElement.isJsonNull() && chestElement.isJsonObject()) {
			JsonObject chestObject = chestElement.getAsJsonObject();
			KingdomChest chest = new KingdomChest(kingdom);
			JsonElement contentsElement = chestObject.get("contents");
			if (contentsElement != null && !contentsElement.isJsonNull() && contentsElement.isJsonObject()) {
				JsonObject contentsObject = contentsElement.getAsJsonObject();
				ItemStackSerializer itemSerializer = new ItemStackSerializer();
				Map<Integer, ItemStack> contents = new HashMap<>();
				for (Entry<String, JsonElement> entry : contentsObject.entrySet()) {
					JsonElement element = contentsObject.get(entry.getKey());
					if (element == null || element.isJsonNull())
						continue;
					contents.put(Integer.parseInt(entry.getKey()), itemSerializer.deserialize(entry.getValue(), ItemStack.class, context));
				}
				chest.setContents(contents);
			}
			kingdom.setKingdomChest(chest);
		}
		return handler.deserialize(kingdom, object, context);
	}

}
