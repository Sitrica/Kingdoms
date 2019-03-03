package com.songoda.kingdoms.command.commands.user;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.command.AbstractCommand;
import com.songoda.kingdoms.manager.managers.RankManager.Rank;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.objects.structures.Structure;
import com.songoda.kingdoms.objects.structures.StructureType;
import com.songoda.kingdoms.placeholders.Placeholder;
import com.songoda.kingdoms.utils.IntervalUtils;
import com.songoda.kingdoms.utils.MessageBuilder;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandUnclaim extends AbstractCommand {

	static {
		registerCommand(new CommandUnclaim());
	}
	
	private final Set<KingdomPlayer> confirmations = new HashSet<>();
	
	protected CommandUnclaim() {
		super("unclaim", false);
	}

	@Override
	protected ReturnType runCommand(Kingdoms instance, CommandSender sender, String... arguments) {
		Player player = (Player) sender;
		KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
		Kingdom kingdom = kingdomPlayer.getKingdom();
		if (kingdom == null) {
			new MessageBuilder("claiming.no-kingdom")
					.setPlaceholderObject(kingdomPlayer)
					.send(player);
			return ReturnType.FAILURE;
		}
		if (!kingdom.getPermissions(kingdomPlayer.getRank()).canUnclaim()) {
			new MessageBuilder("kingdoms.rank-too-low-unclaim-override")
					.withPlaceholder(kingdom.getLowestRankFor(rank -> rank.canClaim()), new Placeholder<Optional<Rank>>("%rank%") {
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
		landManager.playerClaimLand(kingdomPlayer);
		if (arguments.length > 0) {
			if (arguments[0].equalsIgnoreCase("all")) {
				if (confirmations.contains(kingdomPlayer)) {
					confirmations.remove(kingdomPlayer);
					int amount = landManager.unclaimAllLand(kingdom);
					if (amount == -1) {
						new MessageBuilder("commands.unclaim.processing")
								.setPlaceholderObject(kingdomPlayer)
								.setKingdom(kingdom)
								.send(player);
						return ReturnType.FAILURE;
					}
					new MessageBuilder("commands.unclaim.total")
							.setPlaceholderObject(kingdomPlayer)
							.replace("%amount%", amount)
							.setKingdom(kingdom)
							.send(player);
				} else {
					confirmations.add(kingdomPlayer);
					new MessageBuilder("commands.unclaim.confirmation")
							.setPlaceholderObject(kingdomPlayer)
							.setKingdom(kingdom)
							.send(player);
					Location nexus = kingdom.getNexusLocation();
					if (nexus == null)
						return ReturnType.FAILURE;
					Land nexusLand = landManager.getLand(nexus.getChunk());
					if (invadingManager.isBeingInvaded(nexusLand))
						new MessageBuilder("commands.unclaim.unclaim-all-nexus")
								.setPlaceholderObject(kingdomPlayer)
								.setKingdom(kingdom)
								.send(player);
					String interval = configuration.getString("claiming.unclaim-all-confirmation-delay", "25 seconds");
					Bukkit.getScheduler().runTaskLaterAsynchronously(instance, new Runnable() {
						@Override
						public void run() {
							if (confirmations.contains(kingdomPlayer)) {
								confirmations.remove(kingdomPlayer);
								new MessageBuilder("commands.unclaim.confirmation-expired")
										.setPlaceholderObject(kingdomPlayer)
										.setKingdom(kingdom)
										.send(player);
							}
						}
					}, IntervalUtils.getInterval(interval));
				}
			} else if (arguments[0].equalsIgnoreCase("disconnected")) {
				int amount = landManager.unclaimDisconnectedLand(kingdom);
				if (amount == -1) {
					new MessageBuilder("commands.unclaim.processing")
							.setPlaceholderObject(kingdomPlayer)
							.setKingdom(kingdom)
							.send(player);
					return ReturnType.FAILURE;
				}
				new MessageBuilder("commands.unclaim.total")
						.setPlaceholderObject(kingdomPlayer)
						.replace("%amount%", amount)
						.setKingdom(kingdom)
						.send(player);
			}
		} else {
			Chunk chunk = kingdomPlayer.getLocation().getChunk();
			Land land = landManager.getLand(chunk);
			OfflineKingdom landKingdom = land.getKingdomOwner();
			if (landKingdom == null || !landKingdom.equals(landKingdom)) {
				new MessageBuilder("commands.unclaim.not-your-kingdom")
						.setPlaceholderObject(kingdomPlayer)
						.setKingdom(landKingdom)
						.send(player);
				return ReturnType.FAILURE;
			}
			Structure structure = land.getStructure();
			if (structure != null && structure.getType() == StructureType.NEXUS) {
				new MessageBuilder("commands.unclaim.cannot-unclaim-nexus")
						.setPlaceholderObject(kingdomPlayer)
						.setKingdom(landKingdom)
						.send(player);
				return ReturnType.FAILURE;
			}
			landManager.unclaimLand(kingdom, chunk);
			new MessageBuilder("commands.unclaim.success")
					.setPlaceholderObject(kingdomPlayer)
					.setKingdom(landKingdom)
					.send(player);
		}
		return ReturnType.SUCCESS;
	}

	@Override
	protected String getConfigurationNode() {
		return "unclaim";
	}

	@Override
	public String getPermissionNode() {
		return "kingdoms.unclaim";
	}

}
