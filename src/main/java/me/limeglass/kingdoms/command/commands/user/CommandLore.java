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
import me.limeglass.kingdoms.utils.Utils;

public class CommandLore extends AbstractCommand {

	public CommandLore() {
		super(false, "description", "desc", "lore", "l");
	}

	@Override
	protected ReturnType runCommand(Kingdoms instance, CommandSender sender, String... arguments) {
		Player player = (Player) sender;
		KingdomPlayer kingdomPlayer = instance.getManager(PlayerManager.class).getKingdomPlayer(player);
		Kingdom kingdom = kingdomPlayer.getKingdom();
		if (kingdom == null) {
			new MessageBuilder("commands.lore.no-kingdom")
					.setPlaceholderObject(kingdomPlayer)
					.send(player);
			return ReturnType.FAILURE;
		}
		if (!kingdom.getPermissions(kingdomPlayer.getRank()).canSetLore()) {
			new MessageBuilder("commands.lore.rank-too-low-lore")
					.withPlaceholder(kingdom.getLowestRankFor(rank -> rank.canSetLore()), new Placeholder<Optional<Rank>>("%rank%") {
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
		String lore = String.join(" ", arguments);
		int length = configuration.getInt("plugin.max-lore-length", 64);
		if (length > 0 && lore.length() > length) {
			new MessageBuilder("commands.lore.lore-is-to-long")
					.setPlaceholderObject(kingdomPlayer)
					.replace("%lore%", lore)
					.setKingdom(kingdom)
					.send(player);
			return ReturnType.FAILURE;
		}
		if (Utils.checkForMatch(configuration.getStringList("disallowed-kingdom-names"), lore) || lore.contains("$") || lore.contains("%")) {
			new MessageBuilder("commands.lore.blacklisted")
					.setPlaceholderObject(kingdomPlayer)
					.replace("%lore%", lore)
					.setKingdom(kingdom)
					.send(player);
			return ReturnType.FAILURE;
        }
		kingdom.setLore(lore);
		new MessageBuilder("commands.lore.success")
				.setPlaceholderObject(kingdomPlayer)
				.replace("%lore%", lore)
				.send(player);
		return ReturnType.SUCCESS;
	}

	@Override
	public String getConfigurationNode() {
		return "lore";
	}

	@Override
	public String[] getPermissionNodes() {
		return new String[] {"kingdoms.lore", "kingdoms.player"};
	}

}
