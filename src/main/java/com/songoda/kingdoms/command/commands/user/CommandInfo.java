package com.songoda.kingdoms.command.commands.user;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.command.AbstractCommand;
import com.songoda.kingdoms.manager.managers.PlayerManager;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.placeholders.Placeholder;
import com.songoda.kingdoms.utils.ListMessageBuilder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandInfo extends AbstractCommand {

	private final PlayerManager playerManager;

	public CommandInfo() {
		super(false, "info", "i");
		playerManager = instance.getManager("player", PlayerManager.class);
	}

	@Override
	protected ReturnType runCommand(Kingdoms instance, CommandSender sender, String... arguments) {
		Player player = (Player) sender;
		KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
		new ListMessageBuilder("commands.info.info")
				.withPlaceholder("%kingdom%", new Placeholder<KingdomPlayer>() {
					@Override
					public Object replace(KingdomPlayer player) {
						Kingdom kingdom = player.getKingdom();
						return kingdom != null ? kingdom.getName() : "&c&lNo Kingdom";
					}
				})
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
