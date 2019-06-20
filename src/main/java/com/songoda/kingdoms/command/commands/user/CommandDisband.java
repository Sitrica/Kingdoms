package com.songoda.kingdoms.command.commands.user;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.command.AbstractCommand;
import com.songoda.kingdoms.manager.inventories.ConfirmationManager;
import com.songoda.kingdoms.manager.managers.KingdomManager;
import com.songoda.kingdoms.manager.managers.PlayerManager;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.utils.MessageBuilder;

public class CommandDisband extends AbstractCommand {

	public CommandDisband() {
		super(false, "disband");
	}

	@Override
	protected ReturnType runCommand(Kingdoms instance, CommandSender sender, String... arguments) {
		Player player = (Player) sender;
		KingdomPlayer kingdomPlayer = instance.getManager(PlayerManager.class).getKingdomPlayer(player);
		Kingdom kingdom = kingdomPlayer.getKingdom();
		if (kingdom == null) {
			new MessageBuilder("commands.disband.no-kingdom")
					.setPlaceholderObject(kingdomPlayer)
					.send(player);
			return ReturnType.FAILURE;
		}
		if (!kingdom.getOwner().equals(kingdomPlayer)) {
			new MessageBuilder("commands.disband.only-owner")
					.replace("%owner%", kingdom.getOwner().getName())
					.setPlaceholderObject(kingdomPlayer)
					.send(kingdomPlayer);
			return ReturnType.FAILURE;
		}
		instance.getManager(ConfirmationManager.class).openConfirmation(kingdomPlayer, result -> {
			if (!result) {
				new MessageBuilder("commands.disband.cancelled")
						.replace("%owner%", kingdom.getOwner().getName())
						.setPlaceholderObject(kingdomPlayer)
						.send(kingdomPlayer);
				return;
			}
			instance.getManager(KingdomManager.class).deleteKingdom(kingdom.getName());
		});
		return ReturnType.SUCCESS;
	}

	@Override
	public String getConfigurationNode() {
		return "disband";
	}

	@Override
	public String[] getPermissionNodes() {
		return null;
	}

}
