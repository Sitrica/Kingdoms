package me.limeglass.kingdoms.command.commands.admin;

import java.util.Arrays;
import java.util.Optional;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.limeglass.kingdoms.Kingdoms;
import me.limeglass.kingdoms.command.AdminCommand;
import me.limeglass.kingdoms.manager.managers.KingdomManager;
import me.limeglass.kingdoms.manager.managers.PlayerManager;
import me.limeglass.kingdoms.objects.kingdom.OfflineKingdom;
import me.limeglass.kingdoms.objects.player.KingdomPlayer;
import me.limeglass.kingdoms.utils.MessageBuilder;

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
		Optional<OfflineKingdom> optional = kingdomManager.getOfflineKingdom(name);
		if (!optional.isPresent()) {
			new MessageBuilder("commands.resource-points.no-kingdom-found")
					.setPlaceholderObject(kingdomPlayer)
					.replace("%kingdom%", name)
					.send(player);
			return ReturnType.FAILURE;
		}
		OfflineKingdom kingdom = optional.get();
		switch (function) {
			case "subtract":
			case "remove":
				kingdom.subtractResourcePoints(amount);
				new MessageBuilder("commands.resource-points.removed")
						.setPlaceholderObject(kingdomPlayer)
						.replace("%amount%", amount)
						.setKingdom(kingdom)
						.send(kingdomPlayer);
				break;
			case "give":
			case "add":
				kingdom.addResourcePoints(amount);
				new MessageBuilder("commands.resource-points.added")
						.setPlaceholderObject(kingdomPlayer)
						.replace("%amount%", amount)
						.setKingdom(kingdom)
						.send(kingdomPlayer);
				break;
			case "set":
				kingdom.setResourcePoints(amount);
				new MessageBuilder("commands.resource-points.set")
						.setPlaceholderObject(kingdomPlayer)
						.replace("%amount%", amount)
						.setKingdom(kingdom)
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
