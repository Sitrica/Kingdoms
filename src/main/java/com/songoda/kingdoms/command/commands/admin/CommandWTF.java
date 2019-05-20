package com.songoda.kingdoms.command.commands.admin;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.command.AdminCommand;
import com.songoda.kingdoms.inventories.structures.NexusInventory;
import com.songoda.kingdoms.manager.inventories.InventoryManager;
import com.songoda.kingdoms.manager.managers.PlayerManager;
import com.songoda.kingdoms.objects.player.KingdomPlayer;

public class CommandWTF extends AdminCommand {

	public CommandWTF() {
		super(false, "nexus", "n");
	}

	@Override
	protected ReturnType runCommand(Kingdoms instance, CommandSender sender, String... arguments) {
		Player player = (Player) sender;
		if (arguments.length != 0)
			return ReturnType.SYNTAX_ERROR;
		KingdomPlayer kingdomPlayer = instance.getManager(PlayerManager.class).getKingdomPlayer(player);
		instance.getManager(InventoryManager.class).getInventory(NexusInventory.class).open(kingdomPlayer);
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
