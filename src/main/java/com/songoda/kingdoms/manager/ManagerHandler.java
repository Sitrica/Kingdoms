package com.songoda.kingdoms.manager;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.objects.ManagerOptional;
import com.songoda.kingdoms.utils.Utils;

public class ManagerHandler {

	private static Map<String, Manager> managers = new HashMap<>();
	private final transient Kingdoms instance;

	public ManagerHandler(Kingdoms instance) {
		this.instance = instance;
		Utils.loadClasses(instance, instance.getPackageName() + ".manager", "managers");
	}

	public static void registerManager(String name, Manager manager) {
		managers.put(name, manager);
	}
	
	public ManagerOptional<Manager> getManager(String name) {
		return new ManagerOptional<Manager>(name, Optional.ofNullable(managers.get(name)));
	}

	public Map<String, Manager> getManagers() {
		return Collections.unmodifiableMap(managers);
	}
	
	protected Kingdoms getInstance() {
		return instance;
	}

}
