package com.songoda.kingdoms.command.commands.admin;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.command.AdminCommand;
import com.songoda.kingdoms.manager.managers.PlayerManager;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.objects.structures.Extractor;
import com.songoda.kingdoms.objects.structures.Structure;
import com.songoda.kingdoms.objects.structures.StructureType;
import com.songoda.kingdoms.utils.MessageBuilder;

public class CommandExtractor extends AdminCommand {

	public CommandExtractor() {
		super(false, "extractor");
	}

	@Override
	protected ReturnType runCommand(Kingdoms instance, CommandSender sender, String... arguments) {
		Player player = (Player) sender;
		KingdomPlayer kingdomPlayer = instance.getManager(PlayerManager.class).getKingdomPlayer(player);
		Land land = kingdomPlayer.getLandAt();
		Structure structure = land.getStructure();
		if (structure == null || structure.getType() != StructureType.EXTRACTOR) {
			new MessageBuilder("commands.extractor.no-structure-found")
					.setPlaceholderObject(kingdomPlayer)
					.send(kingdomPlayer);
			return ReturnType.FAILURE;
		}
		Extractor extractor = (Extractor) structure;
		extractor.makeReady();
		new MessageBuilder("commands.extractor.ready")
				.setPlaceholderObject(kingdomPlayer)
				.send(kingdomPlayer);
		return ReturnType.SUCCESS;
	}

	@Override
	public String getConfigurationNode() {
		return "extractor";
	}

	@Override
	public String[] getPermissionNodes() {
		return new String[] {"kingdoms.extractor", "kingdoms.admin"};
	}

}
