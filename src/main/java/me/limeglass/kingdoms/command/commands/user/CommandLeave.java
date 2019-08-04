package me.limeglass.kingdoms.command.commands.user;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.limeglass.kingdoms.Kingdoms;
import me.limeglass.kingdoms.command.AbstractCommand;
import me.limeglass.kingdoms.events.MemberLeaveEvent;
import me.limeglass.kingdoms.manager.inventories.ConfirmationManager;
import me.limeglass.kingdoms.manager.managers.KingdomManager;
import me.limeglass.kingdoms.manager.managers.PlayerManager;
import me.limeglass.kingdoms.objects.kingdom.Kingdom;
import me.limeglass.kingdoms.objects.player.KingdomPlayer;
import me.limeglass.kingdoms.utils.MessageBuilder;

public class CommandLeave extends AbstractCommand {

	public CommandLeave() {
		super(false, "leave", "part");
	}

	@Override
	protected ReturnType runCommand(Kingdoms instance, CommandSender sender, String... arguments) {
		Player player = (Player) sender;
		KingdomPlayer kingdomPlayer = instance.getManager(PlayerManager.class).getKingdomPlayer(player);
		Kingdom kingdom = kingdomPlayer.getKingdom();
		if (kingdom == null) {
			new MessageBuilder("claiming.no-kingdom")
					.setPlaceholderObject(kingdomPlayer)
					.send(player);
			return ReturnType.FAILURE;
		}
		if (arguments.length != 0)
			return ReturnType.SYNTAX_ERROR;
		instance.getManager(ConfirmationManager.class).openConfirmation(kingdomPlayer, result -> {
			if (!result) {
				new MessageBuilder("commands.leave.cancelled")
						.setPlaceholderObject(kingdomPlayer)
						.send(kingdomPlayer);
				return;
			}
			kingdomPlayer.onKingdomLeave();
			kingdomPlayer.setKingdom(null);
			kingdomPlayer.setRank(null);
			KingdomManager kingdomManager = instance.getManager(KingdomManager.class);
			kingdomManager.onPlayerLeave(kingdomPlayer, kingdom);
			kingdom.removeMember(kingdomPlayer);
			if (kingdom.getMembers().isEmpty()) {
				kingdomManager.deleteKingdom(kingdom.getName());
				return;
			}
			Bukkit.getPluginManager().callEvent(new MemberLeaveEvent(kingdomPlayer, kingdom));
			new MessageBuilder("commands.leave.leave-broadcast")
					.setPlaceholderObject(kingdomPlayer)
					.send(kingdom.getOnlinePlayers());
		});
		return ReturnType.SUCCESS;
	}

	@Override
	public String getConfigurationNode() {
		return "leave";
	}

	@Override
	public String[] getPermissionNodes() {
		return null;
	}

}
