package com.songoda.kingdoms.command.commands.user;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.command.AbstractCommand;
import com.songoda.kingdoms.manager.managers.PlayerManager;
import com.songoda.kingdoms.manager.managers.ChatManager.ChatChannel;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.utils.ListMessageBuilder;
import com.songoda.kingdoms.utils.MessageBuilder;

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
			ChatChannel channel = ChatChannel.get(input);
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
