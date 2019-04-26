package com.songoda.kingdoms.database.handlers;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.inventory.ItemStack;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.songoda.kingdoms.database.serializers.ItemStackSerializer;
import com.songoda.kingdoms.database.serializers.OfflineKingdomSerializer;
import com.songoda.kingdoms.objects.kingdom.KingdomChest;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;

public class KingdomChestHandler implements Handler<KingdomChest> {

	private final OfflineKingdomSerializer kingdomSerializer;
	private final ItemStackSerializer itemSerializer;

	public KingdomChestHandler() {
		this.kingdomSerializer = new OfflineKingdomSerializer();
		this.itemSerializer = new ItemStackSerializer();
	}

	@Override
	public JsonObject serialize(KingdomChest chest, JsonObject json, JsonSerializationContext context) {
		json.add("kingdom", kingdomSerializer.serialize(chest.getKingdom(), OfflineKingdom.class, context));
		JsonObject contentsObject = new JsonObject();
		Map<Integer, ItemStack> contents = chest.getContents();
		if (contents != null && !contents.isEmpty()) {
			for (Entry<Integer, ItemStack> entry : contents.entrySet()) {
				contentsObject.add(entry.getKey() + "", itemSerializer.serialize(entry.getValue(), ItemStack.class, context));
			}
		}
		json.add("contents", contentsObject);
		return json;
	}

	@Override
	public KingdomChest deserialize(KingdomChest chest, JsonObject json, JsonDeserializationContext context) {
		JsonElement kingdomElement = json.get("kingdom");
		if (kingdomElement == null || kingdomElement.isJsonNull())
			return null;
		OfflineKingdom kingdom = kingdomSerializer.deserialize(kingdomElement, OfflineKingdom.class, context);
		if (kingdom == null)
			return null;
		chest = new KingdomChest(kingdom);
		JsonElement chestElement = json.get("contents");
		if (chestElement == null || chestElement.isJsonNull() || !chestElement.isJsonObject())
			return chest;
		JsonObject chestObject = chestElement.getAsJsonObject();
		Map<Integer, ItemStack> contents = new HashMap<>();
		for (Entry<String, JsonElement> entry : chestObject.entrySet()) {
			JsonElement element = chestObject.get(entry.getKey());
			if (element == null || element.isJsonNull())
				continue;
			contents.put(Integer.parseInt(entry.getKey()), itemSerializer.deserialize(entry.getValue(), ItemStack.class, context));
		}
		chest.setContents(contents);
		return chest;
	}

}
