package com.songoda.kingdoms.command.commands.user;

import java.util.Optional;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.command.AbstractCommand;
import com.songoda.kingdoms.manager.managers.KingdomManager;
import com.songoda.kingdoms.manager.managers.PlayerManager;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.utils.ListMessageBuilder;
import com.songoda.kingdoms.utils.MessageBuilder;

public class CommandInfo extends AbstractCommand {

	private final KingdomManager kingdomManager;
	private final PlayerManager playerManager;

	public CommandInfo() {
		super(false, "info", "show", "i");
		this.playerManager = instance.getManager("player", PlayerManager.class);
		this.kingdomManager = instance.getManager("kingdom", KingdomManager.class);
	}

	@Override
	protected ReturnType runCommand(Kingdoms instance, CommandSender sender, String... arguments) {
		Player player = (Player) sender;
		KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
		if (arguments.length > 0) {
			String name = String.join(" ", arguments);
			Optional<Kingdom> kingdom = kingdomManager.getKingdom(name);
			if (!kingdom.isPresent()) {
				new MessageBuilder("commands.info.no-kingdom-found")
						.setPlaceholderObject(kingdomPlayer)
						.send(player);
				return ReturnType.FAILURE;
			}
			new ListMessageBuilder(false, "commands.info.info")
					.setKingdom(kingdom.get())
					.send(player);
			return ReturnType.SUCCESS;
		}
		new ListMessageBuilder(false, "commands.info.info")
				.setKingdom(kingdomPlayer.getKingdom())
				.setPlaceholderObject(kingdomPlayer)
				.send(player);
		return ReturnType.SUCCESS;
	}

	@Override
	public String getConfigurationNode() {
		return "info";
	}

	@Override
	public String getPermissionNode() {
		return "kingdoms.info";
	}

}
