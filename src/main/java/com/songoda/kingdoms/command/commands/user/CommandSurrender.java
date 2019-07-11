package com.songoda.kingdoms.command.commands.user;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.Sets;
import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.command.AbstractCommand;
import com.songoda.kingdoms.manager.managers.InvadingManager;
import com.songoda.kingdoms.manager.managers.PlayerManager;
import com.songoda.kingdoms.objects.invasions.Invasion;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.utils.ListMessageBuilder;
import com.songoda.kingdoms.utils.MessageBuilder;

public class CommandSurrender extends AbstractCommand {

	public CommandSurrender() {
		super(false, "surrender", "forfeit", "ff");
	}

	@Override
	protected ReturnType runCommand(Kingdoms instance, CommandSender sender, String... arguments) {
		Player player = (Player) sender;
		KingdomPlayer kingdomPlayer = instance.getManager(PlayerManager.class).getKingdomPlayer(player);
		Kingdom kingdom = kingdomPlayer.getKingdom();
		if (kingdom == null) {
			new MessageBuilder("commands.surrender.no-kingdom")
					.setPlaceholderObject(kingdomPlayer)
					.send(player);
			return ReturnType.FAILURE;
		}
		InvadingManager invadingManager = instance.getManager(InvadingManager.class);
		Set<Invasion> invasions = Sets.newHashSet(invadingManager.getInvasionsOn(kingdomPlayer));
		if (invasions.isEmpty()) {
			new MessageBuilder("commands.surrender.no-invasions")
					.setPlaceholderObject(kingdomPlayer)
					.send(player);
			return ReturnType.FAILURE;
		}
		if (arguments.length != 0) {
			arguments = Arrays.copyOfRange(arguments, 1, arguments.length);
			String name = String.join(" ", arguments);
			invasions.removeIf(invasion -> {
				Optional<OfflineKingdom> opponent = invasion.getOpponentTo(kingdom);
				if (!opponent.isPresent())
					return true;
				if (!opponent.get().getName().equalsIgnoreCase(name))
					return true;
				return false;
			});
		}
		invasions.forEach(invasion -> {
			Optional<OfflineKingdom> opponent = invasion.getOpponentTo(kingdom);
			if (!opponent.isPresent())
				return;
			invasion.winner(opponent.get());
			new ListMessageBuilder(false, "commands.surrender.surrender")
					.replace("%kingdom%", kingdom.getName())
					.setPlaceholderObject(kingdomPlayer)
					.setKingdom(kingdom)
					.send(invasion.getInvolved());
		});
		return ReturnType.SUCCESS;
	}

	@Override
	public String getConfigurationNode() {
		return "surrender";
	}

	@Override
	public String[] getPermissionNodes() {
		return new String[] {"kingdoms.player", "kingdoms.surrender"};
	}

}
