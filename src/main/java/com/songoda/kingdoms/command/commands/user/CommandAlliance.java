package com.songoda.kingdoms.command.commands.user;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

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
import com.songoda.kingdoms.utils.IntervalUtils;
import com.songoda.kingdoms.utils.MessageBuilder;

public class CommandAlliance extends AbstractCommand {

	private final Map<String, BukkitTask> tasks = new HashMap<>();
	private final Map<String, String> requests = new HashMap<>();

	public CommandAlliance() {
		super(false, "alliance", "ally", "a");
	}

	@Override
	protected ReturnType runCommand(Kingdoms instance, CommandSender sender, String... arguments) {
		Player player = (Player) sender;
		KingdomPlayer kingdomPlayer = instance.getManager(PlayerManager.class).getKingdomPlayer(player);
		Kingdom kingdom = kingdomPlayer.getKingdom();
		if (kingdom == null) {
			new MessageBuilder("commands.alliance.no-kingdom")
					.setPlaceholderObject(kingdomPlayer)
					.send(player);
			return ReturnType.FAILURE;
		}
		if (!kingdom.getPermissions(kingdomPlayer.getRank()).canAlliance()) {
			new MessageBuilder("commands.alliance.rank-too-low-alliance")
					.withPlaceholder(kingdom.getLowestRankFor(rank -> rank.canAlliance()), new Placeholder<Optional<Rank>>("%rank%") {
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
		if (arguments.length < 1)
			return ReturnType.SYNTAX_ERROR;
		String function = arguments[0];
		if (function.equalsIgnoreCase("accept")) {
			Optional<String> name = requests.entrySet().parallelStream()
					.filter(entry -> entry.getValue().equalsIgnoreCase(kingdom.getName()))
					.map(entry -> entry.getKey())
					.findFirst();
			if (!name.isPresent()) {
				new MessageBuilder("commands.alliance.not-waiting")
						.setPlaceholderObject(kingdomPlayer)
						.setKingdom(kingdom)
						.send(kingdom.getOnlinePlayers());
				return ReturnType.FAILURE;
			}
			Optional<OfflineKingdom> find = instance.getManager(KingdomManager.class).getOfflineKingdom(name.get());
			if (!find.isPresent()) {
				new MessageBuilder("commands.alliance.no-kingdom-found")
						.setPlaceholderObject(kingdomPlayer)
						.replace("%kingdom%", name)
						.send(player);
				return ReturnType.FAILURE;
			}
			OfflineKingdom target = find.get();
			requests.remove(name.get());
			Relation relation = Relation.getRelation(kingdom, target);
			target.addAlliance(kingdom);
			kingdom.addAlliance(target);
			KingdomAllegianceEvent event = new KingdomAllegianceEvent(kingdom, target, relation, Relation.ALLIANCE);
			Bukkit.getPluginManager().callEvent(event);
			new MessageBuilder("commands.alliance.allianced")
					.replace("%player%", player.getName())
					.setKingdom(kingdom)
					.send(target.getKingdom().getOnlinePlayers());
			new MessageBuilder("commands.alliance.allianced")
					.replace("%player%", player.getName())
					.setKingdom(target)
					.send(kingdom.getOnlinePlayers());
			return ReturnType.SUCCESS;
		}
		arguments = Arrays.copyOfRange(arguments, 1, arguments.length);
		String name = String.join(" ", arguments);
		Optional<OfflineKingdom> find = instance.getManager(KingdomManager.class).getOfflineKingdom(name);
		if (!find.isPresent()) {
			new MessageBuilder("commands.alliance.no-kingdom-found")
					.setPlaceholderObject(kingdomPlayer)
					.replace("%kingdom%", name)
					.send(player);
			return ReturnType.FAILURE;
		}
		OfflineKingdom target = find.get();
		if (target.equals(kingdom)) {
			new MessageBuilder("commands.alliance.cant-ally-self")
					.replace("%player%", player.getName())
					.setPlaceholderObject(kingdomPlayer)
					.send(player);
			return ReturnType.FAILURE;
		}
		if (function.equalsIgnoreCase("add")) {
			if (kingdom.isAllianceWith(target)) {
				new MessageBuilder("commands.alliance.already-allianced")
						.replace("%player%", player.getName())
						.setKingdom(target)
						.send(player);
				return ReturnType.FAILURE;
			}
			if (kingdom.isEnemyWith(target)) {
				new MessageBuilder("commands.alliance.is-enemy")
						.replace("%player%", player.getName())
						.setKingdom(target)
						.send(player);
				return ReturnType.FAILURE;
			}
			if (!requests.containsValue(kingdom.getName())) {
				requests.put(kingdom.getName(), target.getName());
				Optional.ofNullable(tasks.get(target.getName())).ifPresent(task -> {
					BukkitTask existing = tasks.remove(target.getName());
					existing.cancel();
				});
				tasks.put(target.getName(), new BukkitRunnable() {
					@Override
					public void run() {
						requests.remove(target.getName());
						tasks.remove(target.getName());
						Bukkit.getScheduler().runTask(instance, () -> {
							new MessageBuilder("commands.alliance.alliance-request-expired")
									.replace("%player%", player.getName())
									.setKingdom(target)
									.send(kingdom.getOnlinePlayers());
							new MessageBuilder("commands.alliance.alliance-request-expired")
									.replace("%player%", player.getName())
									.setKingdom(kingdom)
									.send(target.getKingdom().getOnlinePlayers());
						});
					}
				}.runTaskLaterAsynchronously(instance, IntervalUtils.getInterval(configuration.getString("plugin.alliance-expiration", "10 seconds"))));
				new MessageBuilder("commands.alliance.alliance-request-recieve")
						.replace("%player%", player.getName())
						.setKingdom(target)
						.send(kingdom.getOnlinePlayers());
				new MessageBuilder("commands.alliance.alliance-request-sent")
						.replace("%player%", player.getName())
						.setKingdom(kingdom)
						.send(target.getKingdom().getOnlinePlayers());
				return ReturnType.SUCCESS;
			}
			requests.remove(target.getName());
			Relation relation = Relation.getRelation(kingdom, target);
			target.addAlliance(kingdom);
			kingdom.addAlliance(target);
			KingdomAllegianceEvent event = new KingdomAllegianceEvent(kingdom, target, relation, Relation.ALLIANCE);
			Bukkit.getPluginManager().callEvent(event);
			new MessageBuilder("commands.alliance.allianced")
					.replace("%player%", player.getName())
					.setKingdom(kingdom)
					.send(target.getKingdom().getOnlinePlayers());
			new MessageBuilder("commands.alliance.allianced")
					.replace("%player%", player.getName())
					.setKingdom(target)
					.send(kingdom.getOnlinePlayers());
		} else if (function.equalsIgnoreCase("break") || function.equalsIgnoreCase("remove")) {
			if (!kingdom.isAllianceWith(target)) {
				new MessageBuilder("commands.alliance.not-allianced")
						.replace("%player%", player.getName())
						.setPlaceholderObject(kingdomPlayer)
						.send(player);
				return ReturnType.FAILURE;
			}
			KingdomAllegianceEvent event = new KingdomAllegianceEvent(kingdom, target, Relation.ALLIANCE, Relation.NEUTRAL);
			Bukkit.getPluginManager().callEvent(event);
			kingdom.removeAlliance(target);
			new MessageBuilder("commands.alliance.alliance-removed")
					.replace("%player%", player.getName())
					.setKingdom(kingdom)
					.send(target.getKingdom().getOnlinePlayers());
			new MessageBuilder("commands.alliance.alliance-removed")
					.replace("%player%", player.getName())
					.setKingdom(target)
					.send(kingdom.getOnlinePlayers());
		}
		return ReturnType.SUCCESS;
	}

	@Override
	public String getConfigurationNode() {
		return "alliance";
	}

	@Override
	public String getPermissionNode() {
		return "kingdoms.alliance";
	}

}
