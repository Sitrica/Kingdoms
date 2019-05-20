package com.songoda.kingdoms.command.commands.user;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.command.AbstractCommand;
import com.songoda.kingdoms.manager.managers.KingdomManager;
import com.songoda.kingdoms.manager.managers.PlayerManager;
import com.songoda.kingdoms.manager.managers.VisualizerManager;
import com.songoda.kingdoms.manager.managers.external.VaultManager;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.utils.Formatting;
import com.songoda.kingdoms.utils.InventoryUtil;
import com.songoda.kingdoms.utils.MessageBuilder;
import com.songoda.kingdoms.utils.Utils;

public class CommandCreateKingdom extends AbstractCommand {

	private final Optional<VaultManager> vaultManager;
	private final KingdomManager kingdomManager;

	public CommandCreateKingdom() {
		super(false, "create");
		vaultManager = instance.getExternalManager("vault", VaultManager.class);
		kingdomManager = instance.getManager(KingdomManager.class);
	}

	@Override
	protected ReturnType runCommand(Kingdoms instance, CommandSender sender, String... arguments) {
		Player player = (Player) sender;
		KingdomPlayer kingdomPlayer = instance.getManager(PlayerManager.class).getKingdomPlayer(player);
		Kingdom playerKingdom = kingdomPlayer.getKingdom();
		if (playerKingdom != null) {
			new MessageBuilder("commands.create-kingdom.already-in-kingdom")
					.setPlaceholderObject(kingdomPlayer)
					.setKingdom(playerKingdom)
					.send(player);
			return ReturnType.FAILURE;
		}
		String name = String.join(" ", arguments);
		if (!name.matches("[a-zA-Z0-9_]+") && !configuration.getBoolean("plugin.allow-special-characters", false)) {
			new MessageBuilder("commands.create-kingdom.invalid-name")
					.setPlaceholderObject(kingdomPlayer)
					.replace("%kingdom%", name)
					.replace("%name%", name)
					.send(player);
			return ReturnType.FAILURE;
		}
		if (name.equalsIgnoreCase("safezone")|| name.equalsIgnoreCase("warzone")) {
			new MessageBuilder("commands.create-kingdom.banned-name")
					.setPlaceholderObject(kingdomPlayer)
					.replace("%kingdom%", name)
					.replace("%name%", name)
					.send(player);
			return ReturnType.FAILURE;
		}
		if (name.length() > 16) {
			new MessageBuilder("commands.create-kingdom.name-too-long")
					.setPlaceholderObject(kingdomPlayer)
					.replace("%kingdom%", name)
					.replace("%name%", name)
					.send(player);
			return ReturnType.FAILURE;
		}
		if (Utils.checkForMatch(configuration.getStringList("disallowed-kingdom-names"), name)) {
			new MessageBuilder("commands.create-kingdom.blacklisted")
					.setPlaceholderObject(kingdomPlayer)
					.replace("%kingdom%", name)
					.replace("%name%", name)
					.send(player);
			return ReturnType.FAILURE;
        }
		int cost = configuration.getInt("economy.kingdom-create-cost", 0);
		if (configuration.getBoolean("economy.enabled") && vaultManager.isPresent()) {
			if (cost > 0 && vaultManager.get().getBalance(player) < cost) {
				new MessageBuilder("commands.economy-not-enough")
						.setPlaceholderObject(kingdomPlayer)
						.replace("%kingdom%", name)
						.replace("%name%", name)
						.replace("%cost%", cost)
						.send(player);
				return ReturnType.FAILURE;
			}
		}
		if (kingdomManager.hasKingdom(name)) {
			new MessageBuilder("commands.create-kingdom.name-exists")
					.setPlaceholderObject(kingdomPlayer)
					.replace("%kingdom%", name)
					.replace("%name%", name)
					.send(player);
			return ReturnType.FAILURE;
		}
		if (configuration.getBoolean("economy.kingdom-create-item-cost")) {
			Map<Material, Number> items = new HashMap<>();
			Set<String> messages = new HashSet<>();
			for (String node : configuration.getConfigurationSection("economy.items").getKeys(false)) {
				int amount = Integer.parseInt(node);
				Material material = Utils.materialAttempt(configuration.getString("economy.items." + amount), "DIAMOND");
				if (!InventoryUtil.hasEnough(player, material, amount)) {
					messages.add(amount + " of " + material.name());
					items.put(material, amount);
				}
			}
			if (!messages.isEmpty() && !messages.isEmpty()) {
				new MessageBuilder("commands.create-kingdom.need-more-items")
						.setPlaceholderObject(kingdomPlayer)
						.replace("%kingdom%", name)
						.replace("%name%", name)
						.send(player);
				FileConfiguration messagesConfiguration = instance.getConfiguration("messages").get();
				for (String item : messages) {
					player.sendMessage(Formatting.color(messagesConfiguration.getString("commands.create-kingdom.need-more-items-color", "&7") + item));
				}
				return ReturnType.FAILURE;
			}
			items.entrySet().forEach(entry -> InventoryUtil.removeMaterial(player, entry.getKey(), entry.getValue().intValue()));
		}
		if (configuration.getBoolean("economy.enabled") && cost > 0 && vaultManager.isPresent())
			vaultManager.get().withdraw(player, cost);
		Kingdom kingdom = kingdomManager.createNewKingdom(name, kingdomPlayer);
		if (kingdom != null) {
			instance.getManager(VisualizerManager.class).visualizeLand(kingdomPlayer, player.getLocation().getChunk());
			new MessageBuilder("commands.create-kingdom.create-success")
					.setPlaceholderObject(kingdomPlayer)
					.replace("%kingdom%", name)
					.replace("%name%", name)
					.send(player);
		} else {
			new MessageBuilder("messages.processing")
					.setPlaceholderObject(kingdomPlayer)
					.replace("%kingdom%", name)
					.replace("%name%", name)
					.send(player);
		}
		return ReturnType.SUCCESS;
	}

	@Override
	public String getConfigurationNode() {
		return "create-kingdom";
	}

	@Override
	public String getPermissionNode() {
		return "kingdoms.create";
	}

}
