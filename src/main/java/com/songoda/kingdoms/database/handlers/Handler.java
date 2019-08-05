package com.songoda.kingdoms.database.handlers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

/**
 * Handlers handle Json objects that contain multiple different Kingdom objects like Land, Kingdom, KingdomPlayers etc
 */
public interface Handler<T> {

	public JsonObject serialize(T object, JsonObject json, JsonSerializationContext context);

	public T deserialize(T object, JsonObject json, JsonDeserializationContext context);

}
