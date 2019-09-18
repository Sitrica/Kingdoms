package com.songoda.kingdoms.manager.managers;

import org.bukkit.entity.Player;

import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.utils.Formatting;
import com.songoda.kingdoms.utils.Utils;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public class ActionbarManager extends Manager {

	// Caching
	private final boolean classes, method;

	public ActionbarManager() {
		super(false);
		this.classes = Utils.classExists("net.md_5.bungee.api.ChatMessageType") && Utils.classExists("net.md_5.bungee.api.chat.BaseComponent");
		if (!classes) {
			method = false;
			return;
		}
		this.method = Utils.methodExists(Player.Spigot.class, "sendMessage", ChatMessageType.class, BaseComponent.class);
	}

	public void sendActionBar(Player player, String... messages) {
		if (classes && method) {
			for (String message : messages) {
				player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Formatting.color(message)));
			}
		}
	}

	@Override
	public void initalize() {}

	@Override
	public void onDisable() {}

}
