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

	private final ConfirmationManager confirmationManager;
	private final KingdomManager kingdomManager;
	private final PlayerManager playerManager;

	protected CommandDisband() {
		super(false, "disband");
		this.playerManager = instance.getManager("player", PlayerManager.class);
		this.kingdomManager = instance.getManager("kingdom", KingdomManager.class);
		this.confirmationManager = instance.getManager("confirmation", ConfirmationManager.class);
	}

	@Override
	protected ReturnType runCommand(Kingdoms instance, CommandSender sender, String... arguments) {
		Player player = (Player) sender;
		KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
		Kingdom kingdom = kingdomPlayer.getKingdom();
		if (kingdom == null) {
			new MessageBuilder("commands.disband.no-kingdom")
					.setPlaceholderObject(kingdomPlayer)
					.send(player);
			return ReturnType.FAILURE;
		}
		if (kingdom.getOwner().equals(kingdomPlayer)) {
			new MessageBuilder("commands.disband.only-owner")
					.replace("%owner%", kingdom.getOwner().getName())
					.setPlaceholderObject(kingdomPlayer)
					.send(kingdomPlayer);
			return ReturnType.FAILURE;
		}
		confirmationManager.openConfirmation(player, result -> {
			if (!result) {
				new MessageBuilder("commands.disband.cancelled")
						.replace("%owner%", kingdom.getOwner().getName())
						.setPlaceholderObject(kingdomPlayer)
						.send(kingdomPlayer);
				return;
			}
			if (kingdomManager.deleteKingdom(kingdom))
				new MessageBuilder("commands.disband.disbanded")
						.replace("%owner%", kingdom.getOwner().getName())
						.setPlaceholderObject(kingdomPlayer)
						.send(kingdomPlayer);
		});
		return ReturnType.SUCCESS;
	}

	@Override
	public String getConfigurationNode() {
		return "disband";
	}

	@Override
	public String getPermissionNode() {
		return "kingdoms.disband";
	}

}
