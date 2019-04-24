package com.songoda.kingdoms.database.serializers;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.inventory.ItemStack;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.songoda.kingdoms.database.OfflineKingdomSerializer;
import com.songoda.kingdoms.database.Serializer;
import com.songoda.kingdoms.objects.kingdom.KingdomChest;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;

public class KingdomChestSerializer implements Serializer<KingdomChest> {

	private final OfflineKingdomSerializer kingdomSerializer;
	private final ItemStackSerializer itemSerializer;

	public KingdomChestSerializer(OfflineKingdomSerializer kingdomSerializer) {
		this.itemSerializer = new ItemStackSerializer();
		this.kingdomSerializer = kingdomSerializer;
	}

	@Override
	public JsonElement serialize(KingdomChest chest, Type type, JsonSerializationContext context) {
		JsonObject json = new JsonObject();
		json.add("kingdom", kingdomSerializer.serialize(chest.getKingdom(), OfflineKingdom.class, context));
		JsonObject contents = new JsonObject();
		for (Entry<Integer, ItemStack> entry : chest.getContents().entrySet()) {
			contents.add(entry.getKey() + "", itemSerializer.serialize(entry.getValue(), ItemStack.class, context));
		}
		json.add("contents", contents);
		return json;
	}

	@Override
	public KingdomChest deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
		JsonElement kingdomElement = object.get("kingdom");
		if (kingdomElement == null || kingdomElement.isJsonNull())
			return null;
		OfflineKingdom kingdom = kingdomSerializer.deserialize(kingdomElement, OfflineKingdom.class, context);
		if (kingdom == null)
			return null;
		KingdomChest chest = new KingdomChest(kingdom);
		JsonElement chestElement = object.get("contents");
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
