package com.songoda.kingdoms.command.commands.user;

import java.util.Optional;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.command.AbstractCommand;
import com.songoda.kingdoms.events.KingdomInvadeEvent;
import com.songoda.kingdoms.manager.managers.InvadingManager;
import com.songoda.kingdoms.manager.managers.MasswarManager;
import com.songoda.kingdoms.manager.managers.PlayerManager;
import com.songoda.kingdoms.manager.managers.StructureManager;
import com.songoda.kingdoms.manager.managers.RankManager.Rank;
import com.songoda.kingdoms.objects.invasions.CommandTrigger;
import com.songoda.kingdoms.objects.invasions.Invasion;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.placeholders.Placeholder;
import com.songoda.kingdoms.utils.MessageBuilder;

public class CommandInvade extends AbstractCommand {

	public CommandInvade() {
		super(false, "invade", "raid");
	}

	@Override
	protected ReturnType runCommand(Kingdoms instance, CommandSender sender, String... arguments) {
		Player player = (Player) sender;
		KingdomPlayer kingdomPlayer = instance.getManager(PlayerManager.class).getKingdomPlayer(player);
		Kingdom kingdom = kingdomPlayer.getKingdom();
		if (kingdom == null) {
			new MessageBuilder("commands.invade.no-kingdom")
					.setPlaceholderObject(kingdomPlayer)
					.send(player);
			return ReturnType.FAILURE;
		}
		if (kingdom.isNeutral()) {
			new MessageBuilder("commands.invade.neutral-own")
					.setPlaceholderObject(kingdomPlayer)
					.send(player);
			return ReturnType.FAILURE;
		}
		Land land = kingdomPlayer.getLandAt();
		Optional<OfflineKingdom> optional = land.getKingdomOwner();
		if (!optional.isPresent()) {
			new MessageBuilder("commands.invade.land-has-no-kingdom")
					.setPlaceholderObject(kingdomPlayer)
					.send(player);
			return ReturnType.FAILURE;
		}
		OfflineKingdom target = optional.get();
		if (target.equals(kingdom)) {
			new MessageBuilder("commands.invade.cannot-invade-own")
					.setPlaceholderObject(kingdomPlayer)
					.send(player);
			return ReturnType.FAILURE;
		}
		boolean masswar = instance.getManager(MasswarManager.class).isWarOn();
		if (masswar && instance.getConfig().getBoolean("invading.can-only-invade-during-masswar", false)) {
			new MessageBuilder("commands.invade.mass-war-only")
					.setPlaceholderObject(kingdomPlayer)
					.send(player);
			return ReturnType.FAILURE;
		}
		if (target.isNeutral()) {
			new MessageBuilder("commands.invade.neutral-target")
					.replace("%kingdom%", target.getName())
					.setPlaceholderObject(kingdomPlayer)
					.setKingdom(target)
					.send(player);
			return ReturnType.FAILURE;
		}
		if (!kingdom.getPermissions(kingdomPlayer.getRank()).canInvade()) {
			new MessageBuilder("commands.invade.permissions-too-low")
					.withPlaceholder(kingdom.getLowestRankFor(rank -> rank.canInvade()), new Placeholder<Optional<Rank>>("%rank%") {
						@Override
						public String replace(Optional<Rank> rank) {
							if (rank.isPresent())
								return rank.get().getName();
							return "(Not attainable)";
						}
					})
					.replace("%kingdom%", target.getName())
					.setPlaceholderObject(kingdomPlayer)
					.setKingdom(target)
					.send(player);
			return ReturnType.FAILURE;
		}
		int targetOnlineAllowed = instance.getConfig().getInt("invading.online-players.target", 0);
		if (!target.isOnline() && targetOnlineAllowed > 0) {
			new MessageBuilder("commands.invade.target-none-online")
					.replace("%amount%", targetOnlineAllowed)
					.replace("%kingdom%", target.getName())
					.setPlaceholderObject(kingdomPlayer)
					.setKingdom(target)
					.send(player);
			return ReturnType.FAILURE;
		} else if (target.isOnline() && target.getKingdom().getOnlinePlayers().size() < targetOnlineAllowed) {
			new MessageBuilder("commands.invade.target-not-enough-players")
					.replace("%amount%", targetOnlineAllowed)
					.replace("%kingdom%", target.getName())
					.setPlaceholderObject(kingdomPlayer)
					.setKingdom(target)
					.send(player);
			return ReturnType.FAILURE;
		}
		int kingdomOnlineAllowed = instance.getConfig().getInt("invading.online-players.invadee", 1);
		if (kingdom.getOnlinePlayers().size() < kingdomOnlineAllowed) {
			new MessageBuilder("commands.invade.target-not-enough-players")
					.replace("%amount%", kingdomOnlineAllowed)
					.replace("%kingdom%", target.getName())
					.setPlaceholderObject(kingdomPlayer)
					.setKingdom(target)
					.send(player);
			return ReturnType.FAILURE;
		}
		int maxClaims = instance.getConfig().getInt("claiming.maximum-claims", 50);
		if (maxClaims > 0 && kingdom.getClaims().size() >= maxClaims) {
			new MessageBuilder("commands.invade.max-claims-reached")
					.replace("%kingdom%", target.getName())
					.setPlaceholderObject(kingdomPlayer)
					.replace("%max%", maxClaims)
					.setKingdom(target)
					.send(player);
			return ReturnType.FAILURE;
		}
		int landPerMembers = instance.getConfig().getInt("invading.land-per-member", 5);
		if (landPerMembers > 0 && kingdom.getClaims().size() > landPerMembers * kingdom.getMemberSize() + kingdom.getExtraPurchased()) {
			new MessageBuilder("commands.invade.max-claims-need-members")
					.replace("%kingdom%", target.getName())
					.setPlaceholderObject(kingdomPlayer)
					.replace("%needed%", landPerMembers)
					.setKingdom(target)
					.send(player);
			return ReturnType.FAILURE;
		}
		if (!instance.getManager(StructureManager.class).isInvadeable(kingdomPlayer, land)) { //powercell check
			new MessageBuilder("commands.invade.powercell-present")
					.replace("%kingdom%", target.getName())
					.setPlaceholderObject(kingdomPlayer)
					.setKingdom(target)
					.send(player);
			return ReturnType.FAILURE;
		}
		if (kingdom.isAllianceWith(target) && target.isAllianceWith(kingdom)) {
			new MessageBuilder("commands.invade.allianced")
					.replace("%kingdom%", target.getName())
					.setPlaceholderObject(kingdomPlayer)
					.setKingdom(target)
					.send(player);
			return ReturnType.FAILURE;
		}
		InvadingManager invadingManager = instance.getManager(InvadingManager.class);
		Set<Invasion> kingdomInvasions = invadingManager.getAllInvasions(kingdomPlayer);
		long amount = kingdomInvasions.parallelStream()
				.filter(invasion -> invasion.getAttacking().equals(kingdom))
				.count();
		if (amount >= instance.getConfig().getInt("invading.max-invasions-at-once", 1)) {
			new MessageBuilder("commands.invade.invading")
					.replace("%kingdoms%", kingdomInvasions, invasion -> invasion.getTarget().getName())
					.setPlaceholderObject(kingdomPlayer)
					.setKingdom(target)
					.send(player);
			return ReturnType.FAILURE;
		}
		for (Invasion invasion : invadingManager.getInvasionsAt(land)) {
			if (invasion.getAttacking().equals(kingdom)) {
				// If the current invasion mechanic is the default one, this will be called and if false, the config.yml node is not the default.
				boolean command = invadingManager.getInvasionMechanic().callInvade(new CommandTrigger(invasion, land.toInfo(), kingdomPlayer), kingdomPlayer);
				if (command)
					return ReturnType.FAILURE;
			}
			//TODO support multiple invasions on a single land in the future.
			new MessageBuilder("commands.invade.being-invaded")
					.replace("%kingdom%", invasion.getAttacking().getName())
					.setPlaceholderObject(kingdomPlayer)
					.setKingdom(target)
					.send(player);
			return ReturnType.FAILURE;
		}
		int cost = instance.getConfig().getInt("invanding.invade-cost", 10);
		if (masswar)
			new MessageBuilder("commands.invade.mass-war-free")
					.replace("%kingdom%", target.getName())
					.setPlaceholderObject(kingdomPlayer)
					.setKingdom(target)
					.send(player);
		else if (cost > kingdom.getResourcePoints()) {
			new MessageBuilder("commands.invade.cannot-afford")
					.replace("%kingdom%", target.getName())
					.setPlaceholderObject(kingdomPlayer)
					.replace("%cost%", cost)
					.setKingdom(target)
					.send(player);
			return ReturnType.FAILURE;
		} else {
			kingdom.subtractResourcePoints(cost);
			new MessageBuilder("commands.invade.transation-done")
					.replace("%kingdom%", target.getName())
					.setPlaceholderObject(kingdomPlayer)
					.replace("%cost%", cost)
					.setKingdom(target)
					.send(player);
		}
		KingdomInvadeEvent event = new KingdomInvadeEvent(kingdomPlayer, kingdom, target);
		Bukkit.getServer().getPluginManager().callEvent(event);
		if (event.isCancelled())
			return ReturnType.FAILURE;
		kingdom.setNeutral(false);
		kingdom.setInvaded(true);
		instance.getManager(InvadingManager.class).startInvasion(land, target, kingdomPlayer);
		return null;
	}

	@Override
	public String getConfigurationNode() {
		return "invade";
	}

	@Override
	public String[] getPermissionNodes() {
		return new String[] {"kingdoms.invade", "kingdoms.player"};
	}

}
