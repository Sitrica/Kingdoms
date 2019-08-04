package me.limeglass.kingdoms.command.commands.user;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.limeglass.kingdoms.Kingdoms;
import me.limeglass.kingdoms.command.AbstractCommand;
import me.limeglass.kingdoms.manager.managers.PlayerManager;
import me.limeglass.kingdoms.manager.managers.ChatManager.ChatChannel;
import me.limeglass.kingdoms.objects.player.KingdomPlayer;
import me.limeglass.kingdoms.utils.ListMessageBuilder;
import me.limeglass.kingdoms.utils.MessageBuilder;

public class CommandChat extends AbstractCommand {

	public CommandChat() {
		super(false, "chat", "c");
	}

	@Override
	protected ReturnType runCommand(Kingdoms instance, CommandSender sender, String... arguments) {
		Player player = (Player) sender;
		KingdomPlayer kingdomPlayer = instance.getManager(PlayerManager.class).getKingdomPlayer(player);
		if (arguments.length > 1)
			return ReturnType.SYNTAX_ERROR;
		// Toggle
		if (arguments.length <= 0) {
			switch (kingdomPlayer.getChatChannel()) {
				case ALLY:
					kingdomPlayer.setChatChannel(ChatChannel.PUBLIC);
					break;
				case KINGDOM:
					kingdomPlayer.setChatChannel(ChatChannel.ALLY);
					break;
				case PUBLIC:
					kingdomPlayer.setChatChannel(ChatChannel.KINGDOM);
					break;
			}
		} else {
			String input = arguments[0];
			if (input.equalsIgnoreCase("alliance"))
				input = "ally";
			ChatChannel channel = ChatChannel.valueOf(input.toUpperCase());
			if (channel == null) {
				new MessageBuilder("commands.chat.invalid")
						.setPlaceholderObject(kingdomPlayer)
						.replace("%channel%", arguments[0])
						.send(player);
				return ReturnType.FAILURE;
			}
			kingdomPlayer.setChatChannel(channel);
		}
		new ListMessageBuilder(false, "commands.chat.set")
				.replace("%channel%", kingdomPlayer.getChatChannel().getName())
				.setPlaceholderObject(kingdomPlayer)
				.send(player);
		return ReturnType.SUCCESS;
	}

	@Override
	public String getConfigurationNode() {
		return "chat";
	}

	@Override
	public String[] getPermissionNodes() {
		return new String[] {"kingdoms.chat", "kingdoms.player"};
	}

}
