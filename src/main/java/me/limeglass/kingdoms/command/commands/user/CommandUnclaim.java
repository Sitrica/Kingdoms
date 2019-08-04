package me.limeglass.kingdoms.command.commands.user;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.limeglass.kingdoms.Kingdoms;
import me.limeglass.kingdoms.command.AbstractCommand;
import me.limeglass.kingdoms.manager.managers.InvadingManager;
import me.limeglass.kingdoms.manager.managers.LandManager;
import me.limeglass.kingdoms.manager.managers.PlayerManager;
import me.limeglass.kingdoms.manager.managers.RankManager.Rank;
import me.limeglass.kingdoms.objects.kingdom.Kingdom;
import me.limeglass.kingdoms.objects.kingdom.OfflineKingdom;
import me.limeglass.kingdoms.objects.land.Land;
import me.limeglass.kingdoms.objects.player.KingdomPlayer;
import me.limeglass.kingdoms.objects.structures.Structure;
import me.limeglass.kingdoms.objects.structures.StructureType;
import me.limeglass.kingdoms.placeholders.Placeholder;
import me.limeglass.kingdoms.utils.IntervalUtils;
import me.limeglass.kingdoms.utils.MessageBuilder;

public class CommandUnclaim extends AbstractCommand {

	private final Set<KingdomPlayer> confirmations = new HashSet<>();

	public CommandUnclaim() {
		super(false, "unclaim", "u");
	}

	@Override
	protected ReturnType runCommand(Kingdoms instance, CommandSender sender, String... arguments) {
		Player player = (Player) sender;
		KingdomPlayer kingdomPlayer = instance.getManager(PlayerManager.class).getKingdomPlayer(player);
		LandManager landManager = instance.getManager(LandManager.class);
		if (!kingdomPlayer.hasKingdom()) {
			new MessageBuilder("claiming.no-kingdom")
					.setPlaceholderObject(kingdomPlayer)
					.send(player);
			return ReturnType.FAILURE;
		}
		Kingdom kingdom = kingdomPlayer.getKingdom();
		if (!kingdom.getPermissions(kingdomPlayer.getRank()).canUnclaim()) {
			new MessageBuilder("kingdoms.rank-too-low-unclaim-override")
					.withPlaceholder(kingdom.getLowestRankFor(rank -> rank.canUnclaim()), new Placeholder<Optional<Rank>>("%rank%") {
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
					if (!instance.getManager(InvadingManager.class).getInvasionAt(nexusLand).isEmpty())
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
			Optional<OfflineKingdom> optional = land.getKingdomOwner();
			if (!optional.isPresent())
				return null;
			if (!optional.isPresent()) {
				new MessageBuilder("commands.unclaim.not-your-kingdom")
						.setPlaceholderObject(kingdomPlayer)
						.send(player);
				return ReturnType.FAILURE;
			}
			OfflineKingdom landKingdom = optional.get();
			if (!landKingdom.equals(landKingdom)) {
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
			landManager.unclaimLand(kingdom, land);
			new MessageBuilder("commands.unclaim.success")
					.setPlaceholderObject(kingdomPlayer)
					.setKingdom(landKingdom)
					.send(player);
		}
		return ReturnType.SUCCESS;
	}

	@Override
	public String getConfigurationNode() {
		return "unclaim";
	}

	@Override
	public String[] getPermissionNodes() {
		return new String[] {"kingdoms.unclaim", "kingdoms.player"};
	}

}
