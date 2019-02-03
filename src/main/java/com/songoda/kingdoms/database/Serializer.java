package com.songoda.kingdoms.database;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;

public interface Serializer<T> extends JsonDeserializer<T>, JsonSerializer<T> {
	
}
