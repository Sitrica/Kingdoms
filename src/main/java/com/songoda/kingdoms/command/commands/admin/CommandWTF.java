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

	private final InventoryManager inventoryManager;
	private final PlayerManager playerManager;

	public CommandWTF() {
		super(false, "nexus", "n");
		playerManager = instance.getManager("player", PlayerManager.class);
		inventoryManager = instance.getManager("inventory", InventoryManager.class);
	}

	@Override
	protected ReturnType runCommand(Kingdoms instance, CommandSender sender, String... arguments) {
		Player player = (Player) sender;
		if (arguments.length != 0)
			return ReturnType.SYNTAX_ERROR;
		KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
		inventoryManager.getInventory(NexusInventory.class).open(kingdomPlayer);
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
