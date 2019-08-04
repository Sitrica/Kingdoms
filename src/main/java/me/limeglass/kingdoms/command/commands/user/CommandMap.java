package me.limeglass.kingdoms.command.commands.user;

import java.util.Optional;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.limeglass.kingdoms.Kingdoms;
import me.limeglass.kingdoms.command.AbstractCommand;
import me.limeglass.kingdoms.manager.managers.LandManager;
import me.limeglass.kingdoms.manager.managers.MapManager;
import me.limeglass.kingdoms.manager.managers.PlayerManager;
import me.limeglass.kingdoms.objects.kingdom.Kingdom;
import me.limeglass.kingdoms.objects.kingdom.OfflineKingdom;
import me.limeglass.kingdoms.objects.land.Land;
import me.limeglass.kingdoms.objects.player.KingdomPlayer;
import me.limeglass.kingdoms.objects.structures.Structure;
import me.limeglass.kingdoms.objects.structures.StructureType;

public class CommandMap extends AbstractCommand {

	public CommandMap() {
		super(false, "map", "m");
	}

	@Override
	protected ReturnType runCommand(Kingdoms instance, CommandSender sender, String... arguments) {
		Player player = (Player) sender;
		KingdomPlayer kingdomPlayer = instance.getManager(PlayerManager.class).getKingdomPlayer(player);
		Land land = instance.getManager(LandManager.class).getLandAt(player.getLocation());
		Optional<OfflineKingdom> landOwner = land.getKingdomOwner();
		Kingdom kingdom = kingdomPlayer.getKingdom();
		MapManager mapManager = instance.getManager(MapManager.class);
		if (landOwner.isPresent() && kingdom != null) {
			if (landOwner.get().equals(kingdom)) {
				Structure structure = land.getStructure();
				if (structure != null && structure.getType() == StructureType.RADAR) {
					mapManager.displayMap(kingdomPlayer, true);
					return ReturnType.SUCCESS;
				}
			}
		}
		mapManager.displayMap(kingdomPlayer, false);
		return ReturnType.SUCCESS;
	}

	@Override
	public String getConfigurationNode() {
		return "map";
	}

	@Override
	public String[] getPermissionNodes() {
		boolean permission = instance.getConfiguration("map").get().getBoolean("configure.requires-permission", false);
		return permission ? new String[] {"kingdoms.map", "kingdoms.player"} : null;
	}

}
