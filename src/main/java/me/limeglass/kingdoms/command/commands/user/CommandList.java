package me.limeglass.kingdoms.command.commands.user;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.limeglass.kingdoms.Kingdoms;
import me.limeglass.kingdoms.command.AbstractCommand;
import me.limeglass.kingdoms.inventories.ListMenu;
import me.limeglass.kingdoms.manager.inventories.InventoryManager;
import me.limeglass.kingdoms.manager.managers.PlayerManager;
import me.limeglass.kingdoms.objects.player.KingdomPlayer;

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
	public String[] getPermissionNodes() {
		return new String[] {"kingdoms.list", "kingdoms.player"};
	}

}
