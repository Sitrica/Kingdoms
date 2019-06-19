package com.songoda.kingdoms.command.commands.user;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.command.AbstractCommand;
import com.songoda.kingdoms.inventories.ListMenu;
import com.songoda.kingdoms.manager.inventories.InventoryManager;
import com.songoda.kingdoms.manager.managers.PlayerManager;
import com.songoda.kingdoms.objects.player.KingdomPlayer;

public class CommandList extends AbstractCommand {

	public CommandList() {
		super(false, "list", "find", "l");
	}

	@Override
	protected ReturnType runCommand(Kingdoms instance, CommandSender sender, String... arguments) {
		KingdomPlayer kingdomPlayer = instance.getManager(PlayerManager.class).getKingdomPlayer((Player) sender);
		instance.getManager(InventoryManager.class).getInventory(ListMenu.class).open(kingdomPlayer);
		return ReturnType.SUCCESS;
	}

	@Override
	public String getConfigurationNode() {
		return "list";
	}

	@Override
	public String getPermissionNode() {
		return "kingdoms.list";
	}

}
