package com.songoda.kingdoms.manager;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.objects.ManagerOptional;
import com.songoda.kingdoms.utils.Utils;

public class ManagerHandler {

	private final Set<Manager> managers = new HashSet<>();
	
	public ManagerHandler(Kingdoms instance) {
		Utils.getClassesOf(instance, instance.getPackageName(), Manager.class).forEach(clazz -> {
			try {
				Manager manager = clazz.newInstance();
				managers.add(manager);
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		});
	}

	public void registerManager(Manager manager) {
		if (!getManager(manager.getName()).isPresent())
			managers.add(manager);
	}
	
	public ManagerOptional<Manager> getManager(String name) {
		return new ManagerOptional<Manager>(managers.parallelStream()
				.filter(manager -> manager.getName().equalsIgnoreCase(name))
				.findFirst());
	}

	public Set<Manager> getManagers() {
		return Collections.unmodifiableSet(managers);
	}

}
