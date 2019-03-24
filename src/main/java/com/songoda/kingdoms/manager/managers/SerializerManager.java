package com.songoda.kingdoms.manager.managers;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.songoda.kingdoms.database.Serializer;
import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.utils.Utils;

public class SerializerManager extends Manager {
	
	private final Set<Serializer<?>> serializers = new HashSet<>();
	
	protected SerializerManager() {
		super("serializer", false);
		Utils.getClassesOf(instance, instance.getPackageName(), Serializer.class).forEach(serializer -> {
			try {
				serializers.add(serializer.newInstance());
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	public <T> Optional<Serializer<T>> getSerializer(T type) {
		return serializers.parallelStream()
				.filter(serializer -> serializer.getType().getClass().equals(type.getClass()))
				.map(serializer -> (Serializer<T>) serializer)
				.findFirst();
	}
	
	public Set<Serializer<?>> getSerializers() {
		return serializers;
	}

	@Override
	public void onDisable() {
		serializers.clear();
	}

}
