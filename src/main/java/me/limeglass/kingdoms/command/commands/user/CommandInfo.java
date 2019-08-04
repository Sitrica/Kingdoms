package me.limeglass.kingdoms.command.commands.user;

import java.util.Optional;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.limeglass.kingdoms.Kingdoms;
import me.limeglass.kingdoms.command.AbstractCommand;
import me.limeglass.kingdoms.manager.managers.KingdomManager;
import me.limeglass.kingdoms.manager.managers.PlayerManager;
import me.limeglass.kingdoms.objects.kingdom.Kingdom;
import me.limeglass.kingdoms.objects.kingdom.OfflineKingdom;
import me.limeglass.kingdoms.objects.player.KingdomPlayer;
import me.limeglass.kingdoms.utils.ListMessageBuilder;
import me.limeglass.kingdoms.utils.MessageBuilder;

public class CommandInfo extends AbstractCommand {

	public CommandInfo() {
		super(false, "info", "show", "i");
	}

	@Override
	protected ReturnType runCommand(Kingdoms instance, CommandSender sender, String... arguments) {
		Player player = (Player) sender;
		KingdomPlayer kingdomPlayer = instance.getManager(PlayerManager.class).getKingdomPlayer(player);
		if (arguments.length > 0) {
			String name = String.join(" ", arguments);
			Optional<OfflineKingdom> find = instance.getManager(KingdomManager.class).getOfflineKingdom(name);
			if (!find.isPresent()) {
				new MessageBuilder("commands.info.no-kingdom-found")
						.setPlaceholderObject(kingdomPlayer)
						.replace("%kingdom%", name)
						.send(player);
				return ReturnType.FAILURE;
			}
			new ListMessageBuilder(false, "commands.info.info")
					.setKingdom(find.get())
					.send(player);
			return ReturnType.SUCCESS;
		}
		Kingdom kingdom = kingdomPlayer.getKingdom();
		if (kingdom == null) {
			new MessageBuilder("commands.info.no-kingdom")
					.setPlaceholderObject(kingdomPlayer)
					.send(player);
			return ReturnType.FAILURE;
		}
		new ListMessageBuilder(false, "commands.info.info")
				.setPlaceholderObject(kingdomPlayer)
				.setKingdom(kingdom)
				.send(player);
		return ReturnType.SUCCESS;
	}

	@Override
	public String getConfigurationNode() {
		return "info";
	}

	@Override
	public String[] getPermissionNodes() {
		return new String[] {"kingdoms.info", "kingdoms.player"};
	}

}
