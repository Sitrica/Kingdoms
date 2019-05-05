package com.songoda.kingdoms.command.commands.admin;

import java.util.Optional;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.command.AdminCommand;
import com.songoda.kingdoms.manager.inventories.ConfirmationManager;
import com.songoda.kingdoms.manager.managers.KingdomManager;
import com.songoda.kingdoms.manager.managers.PlayerManager;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.utils.MessageBuilder;

public class CommandDelete extends AdminCommand {

	private final ConfirmationManager confirmationManager;
	private final KingdomManager kingdomManager;
	private final PlayerManager playerManager;

	public CommandDelete() {
		super(false, "delete", "remove", "r", "d");
		playerManager = instance.getManager("player", PlayerManager.class);
		kingdomManager = instance.getManager("kingdom", KingdomManager.class);
		confirmationManager = instance.getManager("confirmation", ConfirmationManager.class);
	}

	@Override
	protected ReturnType runCommand(Kingdoms instance, CommandSender sender, String... arguments) {
		Player player = (Player) sender;
		if (arguments.length == 0)
			return ReturnType.SYNTAX_ERROR;
		KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
		String string = String.join(" ", arguments);
		Optional<OfflineKingdom> kingdom = kingdomManager.getOfflineKingdom(string);
		if (!kingdom.isPresent()) {
			new MessageBuilder("commands.delete.no-kingdom-found")
					.setPlaceholderObject(kingdomPlayer)
					.replace("%kingdom%", string)
					.send(player);
			return ReturnType.FAILURE;
		}
		confirmationManager.openConfirmation(kingdomPlayer, result -> {
			player.closeInventory();
			if (!result) {
				new MessageBuilder("commands.delete.cancelled")
						.setPlaceholderObject(kingdomPlayer)
						.replace("%kingdom%", string)
						.send(kingdomPlayer);
				return;
			}
			kingdomManager.deleteKingdom(kingdom.get());
			new MessageBuilder("commands.delete.deleted")
					.setPlaceholderObject(kingdomPlayer)
					.replace("%kingdom%", string)
					.send(kingdomPlayer);
		});
		return ReturnType.SUCCESS;
	}

	@Override
	public String getConfigurationNode() {
		return "delete";
	}

	@Override
	public String getPermissionNode() {
		return "kingdoms.delete";
	}

}
