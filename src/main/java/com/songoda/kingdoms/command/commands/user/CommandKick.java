package com.songoda.kingdoms.command.commands.user;

import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.command.AbstractCommand;
import com.songoda.kingdoms.events.MemberLeaveEvent;
import com.songoda.kingdoms.manager.managers.KingdomManager;
import com.songoda.kingdoms.manager.managers.PlayerManager;
import com.songoda.kingdoms.manager.managers.RankManager.Rank;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.objects.player.OfflineKingdomPlayer;
import com.songoda.kingdoms.placeholders.Placeholder;
import com.songoda.kingdoms.utils.MessageBuilder;

public class CommandKick extends AbstractCommand {

	public CommandKick() {
		super(false, "kick", "k");
	}

	@Override
	protected ReturnType runCommand(Kingdoms instance, CommandSender sender, String... arguments) {
		Player player = (Player) sender;
		KingdomPlayer kingdomPlayer = instance.getManager(PlayerManager.class).getKingdomPlayer(player);
		Kingdom kingdom = kingdomPlayer.getKingdom();
		if (kingdom == null) {
			new MessageBuilder("commands.kick.no-kingdom")
					.setPlaceholderObject(kingdomPlayer)
					.send(player);
			return ReturnType.FAILURE;
		}
		if (!kingdom.getPermissions(kingdomPlayer.getRank()).canKick()) {
			new MessageBuilder("commands.kick.rank-too-low-kick")
					.withPlaceholder(kingdom.getLowestRankFor(rank -> rank.canKick()), new Placeholder<Optional<Rank>>("%rank%") {
						@Override
						public String replace(Optional<Rank> rank) {
							if (rank.isPresent())
								return rank.get().getName();
							return "(Not attainable)";
						}
					})
					.setKingdom(kingdom)
					.send(player);
			return ReturnType.FAILURE;
		}
		if (arguments.length != 1)
			return ReturnType.SYNTAX_ERROR;
		if (player.getName().equalsIgnoreCase(arguments[0])) {
			new MessageBuilder("commands.kick.cant-kick-self")
					.setPlaceholderObject(kingdomPlayer)
					.send(player);
			return ReturnType.FAILURE;
		}
		Optional<OfflineKingdomPlayer> optional = kingdom.getMembers().parallelStream()
				.filter(member -> member.getName().equalsIgnoreCase(arguments[0]))
				.findFirst();
		if (!optional.isPresent()) {
			new MessageBuilder("commands.kick.player-not-found")
					.setPlaceholderObject(kingdomPlayer)
					.replace("%input%", arguments[0])
					.send(player);
			return ReturnType.FAILURE;
		}
		OfflineKingdomPlayer target = optional.get();
		if (!target.hasKingdom()) {
			new MessageBuilder("commands.kick.target-no-kingdom")
					.setPlaceholderObject(kingdomPlayer)
					.replace("%input%", arguments[0])
					.send(player);
			return ReturnType.FAILURE;
		}
		if (!target.getKingdom().equals(kingdom)) {
			new MessageBuilder("commands.kick.targetg-not-in-kingdom")
					.setPlaceholderObject(kingdomPlayer)
					.replace("%input%", arguments[0])
					.send(player);
			return ReturnType.FAILURE;
		}
		if (target.getRank().isHigherThan(kingdomPlayer.getRank())) {
			new MessageBuilder("commands.kick.user-more-superior")
					.setPlaceholderObject(kingdomPlayer)
					.replace("%input%", arguments[0])
					.send(player);
			return ReturnType.FAILURE;
		}
		target.onKingdomLeave();
		target.setKingdom(null);
		target.setRank(null);
		instance.getManager(KingdomManager.class).onPlayerLeave(target, kingdom);
		new MessageBuilder("commands.kick.kick-broadcast")
				.setPlaceholderObject(kingdomPlayer)
				.replace("%kicked%", arguments[0])
				.send(kingdom.getOnlinePlayers());
		Bukkit.getPluginManager().callEvent(new MemberLeaveEvent(target, kingdom));
		return ReturnType.SUCCESS;
	}

	@Override
	public String getConfigurationNode() {
		return "kick";
	}

	@Override
	public String[] getPermissionNodes() {
		return new String[] {"kingdoms.kick", "kingdoms.player"};
	}

}
