package com.songoda.kingdoms.command.commands.user;

import java.util.Optional;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.command.AbstractCommand;
import com.songoda.kingdoms.manager.managers.MapManager;
import com.songoda.kingdoms.manager.managers.PlayerManager;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.objects.structures.Structure;
import com.songoda.kingdoms.objects.structures.StructureType;

public class CommandMap extends AbstractCommand {

	public CommandMap() {
		super(false, "map", "m");
	}

	@Override
	protected ReturnType runCommand(Kingdoms instance, CommandSender sender, String... arguments) {
		Player player = (Player) sender;
		KingdomPlayer kingdomPlayer = instance.getManager(PlayerManager.class).getKingdomPlayer(player);
		Land land = kingdomPlayer.getLandAt();
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
