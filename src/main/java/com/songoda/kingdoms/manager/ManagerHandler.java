package com.songoda.kingdoms.manager;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.bukkit.plugin.PluginManager;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.manager.managers.external.CitizensManager;
import com.songoda.kingdoms.objects.ManagerOptional;
import com.songoda.kingdoms.utils.Utils;

public class ManagerHandler {

	private final Set<ExternalManager> externalManagers = new HashSet<>();
	private final Set<Manager> managers = new HashSet<>();
	private final Kingdoms instance;

	public ManagerHandler(Kingdoms instance) {
		this.instance = instance;
	}

	public <T extends Manager> void start() {
		Utils.getClassesOf(instance, instance.getPackageName() + ".manager", Manager.class)
				.parallelStream()
				.filter(clazz -> clazz != Manager.class)
				.map(clazz -> {
					try {
						Manager manager = clazz.newInstance();
						managers.add(manager);
						return manager;
					} catch (InstantiationException | IllegalAccessException e) {
						Kingdoms.consoleMessage("&dManager " + clazz.getName() + " doesn't have a nullable constructor.");
						e.printStackTrace();
					}
					return null;
				})
				.sorted()
				.forEach(manager -> manager.initalize());
		PluginManager pluginManager = instance.getServer().getPluginManager();
		if (pluginManager.isPluginEnabled("Citizens"))
			externalManagers.add(new CitizensManager());
	}

	public Optional<ExternalManager> getExternalManager(String name) {
		return externalManagers.parallelStream()
				.filter(manager -> manager.getName().equalsIgnoreCase(name))
				.findFirst();
	}

	public ManagerOptional<Manager> getManager(String name) {
		return new ManagerOptional<Manager>(managers.parallelStream()
				.filter(manager -> manager.getName().equalsIgnoreCase(name))
				.findFirst());
	}

	public <M extends Manager> M createManager(Class<M> expected) {
		try {
			M manager = expected.newInstance();
			registerManager(manager);
			return manager;
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void registerManager(Manager manager) {
		if (!managers.contains(manager))
			managers.add(manager);
	}

	public Set<ExternalManager> getExternalManagers() {
		return externalManagers;
	}

	public Set<Manager> getManagers() {
		return managers;
	}

}
