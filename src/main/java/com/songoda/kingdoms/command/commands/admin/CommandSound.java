package com.songoda.kingdoms.command.commands.admin;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.command.AdminCommand;
import com.songoda.kingdoms.utils.MessageBuilder;
import com.songoda.kingdoms.utils.SoundPlayer;

public class CommandSound extends AdminCommand {

	public CommandSound() {
		super(false, "sound", "soundtest");
	}

	@Override
	protected ReturnType runCommand(Kingdoms instance, CommandSender sender, String... arguments) {
		Player player = (Player) sender;
		if (arguments.length != 1)
			return ReturnType.SYNTAX_ERROR;
		FileConfiguration sounds = instance.getConfiguration("sounds").get();
		if (!sounds.isConfigurationSection(arguments[0])) {
			new MessageBuilder("commands.sound.no-configuration-section")
					.replace("%input%", arguments[0])
					.setPlaceholderObject(player)
					.send(player);
			return ReturnType.FAILURE;
		}
		new SoundPlayer(sounds.getConfigurationSection(arguments[0])).playTo(player);
		return ReturnType.SUCCESS;
	}

	@Override
	public String getConfigurationNode() {
		return "sound";
	}

	@Override
	public String[] getPermissionNodes() {
		return new String[] {"kingdoms.sound", "kingdoms.admin"};
	}

}
