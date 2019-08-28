package com.songoda.kingdoms.command.commands.admin;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.command.AdminCommand;
import com.songoda.kingdoms.manager.managers.KingdomManager;
import com.songoda.kingdoms.manager.managers.PlayerManager;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.utils.MessageBuilder;

public class CommandResourcePoints extends AdminCommand {

	public CommandResourcePoints() {
		super(false, "points", "resource-points", "rp");
	}

	@Override
	protected ReturnType runCommand(Kingdoms instance, CommandSender sender, String... arguments) {
		Player player = (Player) sender;
		if (arguments.length < 3)
			return ReturnType.SYNTAX_ERROR;
		KingdomPlayer kingdomPlayer = instance.getManager(PlayerManager.class).getKingdomPlayer(player);
		String function = arguments[0];
		int amount = Integer.parseInt(arguments[1]);
		if (amount < 0 || !function.equals("set") && amount == 0) {
			new MessageBuilder("commands.resource-points.not-valid-amount")
					.setPlaceholderObject(kingdomPlayer)
					.send(kingdomPlayer);
			return ReturnType.FAILURE;
		}
		String name = String.join(" ", Arrays.copyOfRange(arguments, 2, arguments.length));
		KingdomManager kingdomManager = instance.getManager(KingdomManager.class);
		Set<OfflineKingdom> kingdoms = new HashSet<>();
		if (!name.equalsIgnoreCase("all")) {
			Optional<OfflineKingdom> optional = kingdomManager.getOfflineKingdom(name);
			if (!optional.isPresent()) {
				new MessageBuilder("commands.resource-points.no-kingdom-found")
						.setPlaceholderObject(kingdomPlayer)
						.replace("%kingdom%", name)
						.send(player);
				return ReturnType.FAILURE;
			}
			kingdoms.add(optional.get());
		} else {
			// Online Kingdoms Only
			kingdoms = kingdomManager.getKingdoms();
		}
		switch (function) {
			case "subtract":
			case "remove":
				kingdoms.forEach(kingdom -> kingdom.subtractResourcePoints(amount));
				new MessageBuilder("commands.resource-points.removed")
						.replace("%kingdoms%", kingdoms, kingdom -> kingdom.getName())
						.replace("%kingdom%", kingdoms, kingdom -> kingdom.getName())
						.setPlaceholderObject(kingdomPlayer)
						.replace("%amount%", amount)
						.send(kingdomPlayer);
				break;
			case "give":
			case "add":
				kingdoms.forEach(kingdom -> kingdom.addResourcePoints(amount));
				new MessageBuilder("commands.resource-points.added")
						.replace("%kingdoms%", kingdoms, kingdom -> kingdom.getName())
						.replace("%kingdom%", kingdoms, kingdom -> kingdom.getName())
						.setPlaceholderObject(kingdomPlayer)
						.replace("%amount%", amount)
						.send(kingdomPlayer);
				break;
			case "set":
				kingdoms.forEach(kingdom -> kingdom.setResourcePoints(amount));
				new MessageBuilder("commands.resource-points.set")
						.replace("%kingdoms%", kingdoms, kingdom -> kingdom.getName())
						.replace("%kingdom%", kingdoms, kingdom -> kingdom.getName())
						.setPlaceholderObject(kingdomPlayer)
						.replace("%amount%", amount)
						.send(kingdomPlayer);
				break;
		}
		return ReturnType.SUCCESS;
	}

	@Override
	public String getConfigurationNode() {
		return "resource-points";
	}

	@Override
	public String[] getPermissionNodes() {
		return new String[] {"kingdoms.resourcepoints", "kingdoms.admin"};
	}

}
