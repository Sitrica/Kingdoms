package com.songoda.kingdoms.command.commands.user;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.command.AbstractCommand;
import com.songoda.kingdoms.manager.managers.NexusManager;
import com.songoda.kingdoms.manager.managers.PlayerManager;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.objects.structures.Structure;
import com.songoda.kingdoms.utils.MessageBuilder;

public class CommandNexus extends AbstractCommand {

	public CommandNexus() {
		super(false, "nexus", "move", "n");
	}

	@Override
	protected ReturnType runCommand(Kingdoms instance, CommandSender sender, String... arguments) {
		Player player = (Player) sender;
		KingdomPlayer kingdomPlayer = instance.getManager(PlayerManager.class).getKingdomPlayer(player);
		Kingdom kingdom = kingdomPlayer.getKingdom();
		if (kingdom == null) {
			new MessageBuilder("commands.nexus.no-kingdom")
					.setPlaceholderObject(kingdomPlayer)
					.send(player);
			return ReturnType.FAILURE;
		}
		Structure structure = kingdomPlayer.getLandAt().getStructure();
		if (structure == null || structure.getLocation().distance(kingdom.getNexusLocation()) > 1) {
			new MessageBuilder("commands.nexus.not-nexus")
					.setPlaceholderObject(kingdomPlayer)
					.send(player);
			return ReturnType.FAILURE;
		}
		instance.getManager(NexusManager.class).startNexusSet(player.getUniqueId());
		new MessageBuilder("commands.nexus.move")
				.setPlaceholderObject(kingdomPlayer)
				.send(player);
		return ReturnType.SUCCESS;
	}

	@Override
	public String getConfigurationNode() {
		return "nexus";
	}

	@Override
	public String[] getPermissionNodes() {
		return new String[] {"kingdoms.nexus", "kingdoms.player"};
	}

}
