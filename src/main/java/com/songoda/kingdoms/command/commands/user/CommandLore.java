package com.songoda.kingdoms.command.commands.user;

import java.util.Optional;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.command.AbstractCommand;
import com.songoda.kingdoms.manager.managers.PlayerManager;
import com.songoda.kingdoms.manager.managers.RankManager.Rank;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.placeholders.Placeholder;
import com.songoda.kingdoms.utils.MessageBuilder;
import com.songoda.kingdoms.utils.Utils;

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
