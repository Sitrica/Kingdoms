package me.limeglass.kingdoms.command.commands.user;

import java.util.Optional;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.limeglass.kingdoms.Kingdoms;
import me.limeglass.kingdoms.command.AbstractCommand;
import me.limeglass.kingdoms.manager.managers.PlayerManager;
import me.limeglass.kingdoms.manager.managers.RankManager.Rank;
import me.limeglass.kingdoms.objects.kingdom.Kingdom;
import me.limeglass.kingdoms.objects.player.KingdomPlayer;
import me.limeglass.kingdoms.placeholders.Placeholder;
import me.limeglass.kingdoms.utils.MessageBuilder;

public class CommandUndo extends AbstractCommand {

	public CommandUndo() {
		super(false, "undoclaim", "undo");
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
		if (!kingdom.getPermissions(kingdomPlayer.getRank()).canClaim()) {
			new MessageBuilder("kingdoms.permissions-too-low")
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
		if (arguments.length == 0) {
			kingdom.undoClaims(1);
			new MessageBuilder("commands.undo.undid")
					.setPlaceholderObject(kingdomPlayer)
					.replace("%amount%", 1)
					.setKingdom(kingdom)
					.send(player);
			return ReturnType.SUCCESS;
		}
		int amount = Integer.parseInt(arguments[0]);
		if (amount <= 0) {
			new MessageBuilder("commands.undo.not-valid-amount")
					.setPlaceholderObject(kingdomPlayer)
					.replace("%amount%", arguments[0])
					.setKingdom(kingdom)
					.send(player);
			return ReturnType.FAILURE;
		}
		amount = kingdom.undoClaims(amount);
		if (amount <= 0) {
			new MessageBuilder("commands.no-recent-claims")
					.setPlaceholderObject(kingdomPlayer)
					.setKingdom(kingdom)
					.send(player);
			return ReturnType.FAILURE;
		}
		new MessageBuilder("commands.undo.undid")
				.setPlaceholderObject(kingdomPlayer)
				.replace("%amount%", amount)
				.setKingdom(kingdom)
				.send(player);
		return ReturnType.SUCCESS;
	}

	@Override
	public String getConfigurationNode() {
		return "undo";
	}

	@Override
	public String[] getPermissionNodes() {
		return new String[] {"kingdoms.undo", "kingdoms.player"};
	}

}
