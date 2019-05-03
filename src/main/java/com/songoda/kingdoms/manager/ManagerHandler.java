package com.songoda.kingdoms.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.PluginManager;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.manager.managers.external.CitizensManager;
import com.songoda.kingdoms.objects.ManagerOptional;
import com.songoda.kingdoms.placeholders.DefaultPlaceholders;
import com.songoda.kingdoms.utils.Utils;

public class ManagerHandler {

	private final Set<ExternalManager> externalManagers = new HashSet<>();
	private final List<Manager> managers = new ArrayList<>();
	private final Kingdoms instance;

	public ManagerHandler(Kingdoms instance) {
		this.instance = instance;
	}

	public <T extends Manager> void start() {
		PluginManager pluginManager = instance.getServer().getPluginManager();
		if (pluginManager.isPluginEnabled("Citizens"))
			externalManagers.add(new CitizensManager());
		List<Manager> sorted = new ArrayList<>();
		DefaultPlaceholders.initalize();
		for (Class<Manager> clazz : Utils.getClassesOf(instance, instance.getPackageName() + ".manager", Manager.class)) {
			if (clazz == Manager.class)
				continue;
			try {
				Manager manager = clazz.newInstance();
				managers.add(manager);
				sorted.add(manager);
			} catch (InstantiationException | IllegalAccessException e) {
				Kingdoms.consoleMessage("&dManager " + clazz.getName() + " doesn't have a nullable constructor.");
				e.printStackTrace();
				continue;
			}
		}
		Collections.sort(sorted);
		for (Manager manager : sorted) {
			Kingdoms.debugMessage("&dInitalizing manager " + manager.getName());
			manager.initalize();
		}
		for (Manager manager : sorted) {
			if (!manager.hasListener())
				continue;
			try {
				Bukkit.getPluginManager().registerEvents(manager, instance);
			} catch (IllegalPluginAccessException e) {
				Kingdoms.consoleMessage("&dFailed to register listener for " + manager.getName() + " manager.");
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends ExternalManager> Optional<T> getExternalManager(String name) {
		for (ExternalManager manager : externalManagers) {
			if (manager.getName().equalsIgnoreCase(name))
				return (Optional<T>) Optional.of(manager);
		}
		return Optional.empty();
	}

	public ManagerOptional<Manager> getManager(String name) {
		Optional<Manager> optional = Optional.empty();
		for (Manager manager : managers) {
			if (manager.getName().equalsIgnoreCase(name))
				optional = Optional.of(manager);
		}
		return new ManagerOptional<Manager>(optional);
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

	public List<Manager> getManagers() {
		return managers;
	}

	public void clear() {
		managers.clear();
	}

}
