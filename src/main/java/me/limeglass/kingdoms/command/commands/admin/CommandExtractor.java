package me.limeglass.kingdoms.command.commands.admin;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.limeglass.kingdoms.Kingdoms;
import me.limeglass.kingdoms.command.AdminCommand;
import me.limeglass.kingdoms.manager.managers.PlayerManager;
import me.limeglass.kingdoms.objects.land.Land;
import me.limeglass.kingdoms.objects.player.KingdomPlayer;
import me.limeglass.kingdoms.objects.structures.Extractor;
import me.limeglass.kingdoms.objects.structures.Structure;
import me.limeglass.kingdoms.objects.structures.StructureType;
import me.limeglass.kingdoms.utils.MessageBuilder;

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
