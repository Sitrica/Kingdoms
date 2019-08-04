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
import me.limeglass.kingdoms.manager.managers.KingdomManager;
import me.limeglass.kingdoms.objects.kingdom.DefenderInfo;
import me.limeglass.kingdoms.objects.kingdom.DefenderUpgrade;
import me.limeglass.kingdoms.objects.kingdom.OfflineKingdom;

public class DefenderInfoSerializer implements Serializer<DefenderInfo> {

	@Override
	public JsonElement serialize(DefenderInfo upgrade, Type type, JsonSerializationContext context) {
		JsonObject json = new JsonObject();
		JsonObject upgrades = new JsonObject();
		for (DefenderUpgrade upgradeType : DefenderUpgrade.values()) {
			upgrades.addProperty(upgradeType.name(), upgrade.getUpgradeLevel(upgradeType));
		}
		json.add("upgrades", upgrades);
		String kingdom = upgrade.getKingdomName();
		if (kingdom != null)
			json.addProperty("kingdom", kingdom);
		return json;
	}

	@Override
	public DefenderInfo deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
		JsonElement kingdomElement = object.get("kingdom");
		if (kingdomElement == null || kingdomElement.isJsonNull())
			return null;
		Optional<OfflineKingdom> kingdom = Kingdoms.getInstance().getManager(KingdomManager.class).getOfflineKingdom(kingdomElement.getAsString());
		if (!kingdom.isPresent())
			return null;
		DefenderInfo info = new DefenderInfo(kingdom.get());
		JsonElement upgradesElement = object.get("upgrades");
		if (upgradesElement == null || upgradesElement.isJsonNull() || !upgradesElement.isJsonObject())
			return info;
		JsonObject upgradesObject = upgradesElement.getAsJsonObject();
		for (DefenderUpgrade upgradeType : DefenderUpgrade.values()) {
			JsonElement element = upgradesObject.get(upgradeType.name());
			if (element == null || element.isJsonNull())
				continue;
			info.setUpgradeLevel(upgradeType, element.getAsInt());
		}
		return info;
	}

}
