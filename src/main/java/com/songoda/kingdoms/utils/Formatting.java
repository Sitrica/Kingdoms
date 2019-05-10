package com.songoda.kingdoms.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import com.songoda.kingdoms.Kingdoms;

public class Formatting {

	public static String messagesPrefixed(FileConfiguration configuration, String... nodes) {
		Kingdoms instance = Kingdoms.getInstance();
		FileConfiguration messages = instance.getConfiguration("messages").orElse(instance.getConfig());
		String complete = messages.getString("messages.prefix", "&7[&6Kingdoms&7] &r");
		return Formatting.color(complete + messages(configuration, Arrays.copyOfRange(nodes, 0, nodes.length)));
	}

	public static String messages(FileConfiguration configuration, String... nodes) {
		String complete = "";
		List<String> list = Arrays.asList(nodes);
		Collections.reverse(list);
		for (String node : list.toArray(new String[list.size()])) {
			complete = configuration.getString(node, "Not set '" + Arrays.toString(nodes) + "'") + " " + complete;
		}
		return Formatting.color(complete);
	}

	public static String getPrefix() {
		Kingdoms instance = Kingdoms.getInstance();
		FileConfiguration messages = instance.getConfiguration("messages").orElse(instance.getConfig());
		return Formatting.color(messages.getString("messages.prefix", "&7[&6Kingdoms&7] &r"));
	}

	public static String color(String input) {
		return ChatColor.translateAlternateColorCodes('&', input);
	}

	public static String colorAndStrip(String input) {
		return stripColor(color(input));
	}

	public static String stripColor(String input) {
		return ChatColor.stripColor(input);
	}

}
