package com.songoda.kingdoms.command.commands.user;

import java.util.Arrays;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.command.AbstractCommand;
import com.songoda.kingdoms.events.KingdomAllegianceEvent;
import com.songoda.kingdoms.manager.managers.KingdomManager;
import com.songoda.kingdoms.manager.managers.PlayerManager;
import com.songoda.kingdoms.manager.managers.RankManager.Rank;
import com.songoda.kingdoms.objects.Relation;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.placeholders.Placeholder;
import com.songoda.kingdoms.utils.MessageBuilder;

public class CommandEnemy extends AbstractCommand {

	public CommandEnemy() {
		super(false, "enemy", "e");
	}

	@Override
	protected ReturnType runCommand(Kingdoms instance, CommandSender sender, String... arguments) {
		Player player = (Player) sender;
		KingdomPlayer kingdomPlayer = instance.getManager(PlayerManager.class).getKingdomPlayer(player);
		Kingdom kingdom = kingdomPlayer.getKingdom();
		if (kingdom == null) {
			new MessageBuilder("commands.enemy.no-kingdom")
					.setPlaceholderObject(kingdomPlayer)
					.send(player);
			return ReturnType.FAILURE;
		}
		if (!kingdom.getPermissions(kingdomPlayer.getRank()).canEnemy()) {
			new MessageBuilder("commands.enemy.rank-too-low-enemy")
					.withPlaceholder(kingdom.getLowestRankFor(rank -> rank.canEnemy()), new Placeholder<Optional<Rank>>("%rank%") {
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
		if (arguments.length <= 1)
			return ReturnType.SYNTAX_ERROR;
		String function = arguments[0];
		arguments = Arrays.copyOfRange(arguments, 1, arguments.length);
		String name = String.join(" ", arguments);
		Optional<OfflineKingdom> find = instance.getManager(KingdomManager.class).getOfflineKingdom(name);
		if (!find.isPresent()) {
			new MessageBuilder("commands.enemy.no-kingdom-found")
					.setPlaceholderObject(kingdomPlayer)
					.replace("%kingdom%", name)
					.send(player);
			return ReturnType.FAILURE;
		}
		OfflineKingdom target = find.get();
		if (target.equals(kingdom)) {
			new MessageBuilder("commands.enemy.cant-enemy-self")
					.replace("%player%", player.getName())
					.setPlaceholderObject(kingdomPlayer)
					.send(player);
			return ReturnType.FAILURE;
		}
		if (function.equalsIgnoreCase("add")) {
			if (kingdom.isEnemyWith(target)) {
				new MessageBuilder("commands.enemy.already-enemy")
						.replace("%player%", player.getName())
						.setKingdom(target)
						.send(player);
				return ReturnType.FAILURE;
			}
			if (kingdom.isAllianceWith(target)) {
				new MessageBuilder("commands.enemy.is-allianced")
						.replace("%player%", player.getName())
						.setKingdom(target)
						.send(player);
				return ReturnType.FAILURE;
			}
			Relation relation = Relation.getRelation(kingdom, target);
			target.addEnemy(kingdom);
			kingdom.addEnemy(target);
			KingdomAllegianceEvent event = new KingdomAllegianceEvent(kingdom, target, relation, Relation.ENEMY);
			Bukkit.getPluginManager().callEvent(event);
			new MessageBuilder("commands.enemy.enemy")
					.replace("%player%", player.getName())
					.setKingdom(kingdom)
					.send(target.getKingdom().getOnlinePlayers());
			new MessageBuilder("commands.enemy.enemy")
					.replace("%player%", player.getName())
					.setKingdom(target)
					.send(kingdom.getOnlinePlayers());
		} else if (function.equalsIgnoreCase("break") || function.equalsIgnoreCase("remove")) {
			if (!kingdom.isAllianceWith(target)) {
				new MessageBuilder("commands.enemy.not-enemy")
						.replace("%player%", player.getName())
						.setPlaceholderObject(kingdomPlayer)
						.send(player);
				return ReturnType.FAILURE;
			}
			KingdomAllegianceEvent event = new KingdomAllegianceEvent(kingdom, target, Relation.ENEMY, Relation.NEUTRAL);
			Bukkit.getPluginManager().callEvent(event);
			kingdom.removeAlliance(target);
			new MessageBuilder("commands.enemy.enemy-removed")
					.replace("%player%", player.getName())
					.setKingdom(kingdom)
					.send(target.getKingdom().getOnlinePlayers());
			new MessageBuilder("commands.enemy.enemy-removed")
					.replace("%player%", player.getName())
					.setKingdom(target)
					.send(kingdom.getOnlinePlayers());
		}
		return ReturnType.SUCCESS;
	}

	@Override
	public String getConfigurationNode() {
		return "enemy";
	}

	@Override
	public String[] getPermissionNodes() {
		return new String[] {"kingdoms.enemy", "kingdoms.player"};
	}

}
