package com.songoda.kingdoms.manager.managers;

import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.utils.Formatting;
import com.songoda.kingdoms.utils.Utils;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Server.Spigot;
import org.bukkit.entity.Player;

public class ActionbarManager extends Manager {

	static {
		registerManager("actionbar", new ActionbarManager());
	}
	
	protected ActionbarManager() {
		super(false);
	}
	
	public static void sendActionBar(Player player, String... messages) {
		if (Utils.classExists("net.md_5.bungee.api.ChatMessageType") || Utils.classExists("net.md_5.bungee.api.chat.TextComponent")) {
			if (Utils.methodExists(Spigot.class, "sendMessage", ChatMessageType.class, TextComponent.class)) {
				for (String message : messages) {
					player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Formatting.color(message)));
				}
			}
		}
	}

	@Override
	public void onDisable() {}

}
