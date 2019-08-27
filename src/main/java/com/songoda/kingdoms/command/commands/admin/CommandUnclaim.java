package com.songoda.kingdoms.command.commands.admin;

import java.util.Optional;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.command.AdminCommand;
import com.songoda.kingdoms.manager.managers.LandManager;
import com.songoda.kingdoms.manager.managers.PlayerManager;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.utils.MessageBuilder;

public class CommandUnclaim extends AdminCommand {

	public CommandUnclaim() {
		super(false, "clear");
	}

	@Override
	protected ReturnType runCommand(Kingdoms instance, CommandSender sender, String... arguments) {
		Player player = (Player) sender;
		if (arguments.length > 0)
			return ReturnType.SYNTAX_ERROR;
		KingdomPlayer kingdomPlayer = instance.getManager(PlayerManager.class).getKingdomPlayer(player);
		Land land = kingdomPlayer.getLandAt();
		Optional<OfflineKingdom> kingdom = land.getKingdomOwner();
		if (!kingdom.isPresent()) {
			new MessageBuilder("commands.clear.no-kingdom-found")
					.setPlaceholderObject(kingdomPlayer)
					.send(player);
			return ReturnType.FAILURE;
		}
		LandManager landManager = instance.getManager(LandManager.class);
		landManager.unclaimLand(land);
		new MessageBuilder("commands.clear.cleared")
				.replace("%kingdom%", kingdom.get().getName())
				.setPlaceholderObject(kingdomPlayer)
				.setKingdom(kingdom.get())
				.send(kingdomPlayer);
		return ReturnType.SUCCESS;
	}

	@Override
	public String getConfigurationNode() {
		return "clear";
	}

	@Override
	public String[] getPermissionNodes() {
		return new String[] {"kingdoms.clear", "kingdoms.admin"};
	}

}
