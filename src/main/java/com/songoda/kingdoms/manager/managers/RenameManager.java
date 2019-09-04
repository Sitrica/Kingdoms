package com.songoda.kingdoms.manager.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.songoda.kingdoms.manager.Manager;

public class RenameManager extends Manager {

	private final Map<Player, Consumer<AsyncPlayerChatEvent>> waiting = new HashMap<>();

	public RenameManager() {
		super(true);
	}

	public void rename(Player player, Consumer<AsyncPlayerChatEvent> consumer) {
		waiting.put(player, consumer);
	}

	@EventHandler
	public void onChat(AsyncPlayerChatEvent event) {
		waiting.entrySet().parallelStream()
				.filter(entry -> entry.getKey().equals(event.getPlayer()))
				.forEach(entry -> {
					if (!event.getMessage().equalsIgnoreCase("cancel"))
						entry.getValue().accept(event);
					waiting.remove(entry.getKey());
				});
	}

	@Override
	public void initalize() {}

	@Override
	public void onDisable() {}

}
