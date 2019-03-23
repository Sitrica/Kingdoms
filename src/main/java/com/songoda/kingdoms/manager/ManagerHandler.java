package com.songoda.kingdoms.manager;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.objects.ManagerOptional;
import com.songoda.kingdoms.utils.Utils;

public class ManagerHandler {

	private final Set<Class<? extends Manager>> classes = new HashSet<>();
	private final static Map<String, Manager> managers = new HashMap<>();
	
	public ManagerHandler(Kingdoms instance) {
		classes.addAll(Utils.getClassesOf(instance, instance.getPackageName() + ".manager", Manager.class));
	}

	public static void registerManager(String name, Manager manager) {
		managers.put(name, manager);
	}
	
	public Set<Class<? extends Manager>> getManagerClasses() {
		return classes;
	}
	
	public ManagerOptional<Manager> getManager(String name) {
		return new ManagerOptional<Manager>(name, Optional.ofNullable(managers.get(name)));
	}

	public Map<String, Manager> getManagers() {
		return Collections.unmodifiableMap(managers);
	}

}
