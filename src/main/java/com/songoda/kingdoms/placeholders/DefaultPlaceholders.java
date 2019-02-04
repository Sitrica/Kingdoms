package com.songoda.kingdoms.placeholders;

import java.util.Optional;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.manager.Manager;

public class DefaultPlaceholders extends Manager {

	static {
		registerManager("defaultplaceholders", new DefaultPlaceholders());
		Placeholders.registerPlaceholder(new SimplePlaceholder("%prefix%") {
			@Override
			public String get() {
				Optional<FileConfiguration> messages = Kingdoms.getInstance().getConfiguration("messages");
				if (messages.isPresent())
					return messages.get().getString("messages.prefix", "&7[&6Kingdoms&7] &r");
				return "&7[&6Kingdoms&7] &r";
			}
		});
		Placeholders.registerPlaceholder(new Placeholder<Player>("%player%") {
			@Override
			public String replace(Player player) {
				return player.getName();
			}
		});
		Placeholders.registerPlaceholder(new Placeholder<CommandSender>("%sender%") {
			@Override
			public String replace(CommandSender sender) {
				return sender.getName();
			}
		});
		Placeholders.registerPlaceholder(new Placeholder<String>("%string%") {
			@Override
			public String replace(String string) {
				return string;
			}
		});
	}
	
	protected DefaultPlaceholders() {
		super(false);
	}

	@Override
	public void onDisable() {}
	
}
