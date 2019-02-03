package com.songoda.kingdoms.database.serializers;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.songoda.kingdoms.database.Serializer;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Type;

public class ItemStackArraySerializer implements Serializer<ItemStack[]> {

	@Override
	public JsonElement serialize(ItemStack[] source, Type typeOfSrc, JsonSerializationContext context) {
		JsonArray array = new JsonArray();
		for (ItemStack item : source) {
			array.add(context.serialize(item, ItemStack.class));
		}
		return array;
	}

	@Override
	public ItemStack[] deserialize(JsonElement json, Type type, JsonDeserializationContext context) {
		JsonArray array = json.getAsJsonArray();
		ItemStack[] source = new ItemStack[array.size()];
		for (int i = 0; i < source.length; i++) {
			source[i] = context.deserialize(array.get(i), ItemStack.class);
		}
		return source;
	}

}
