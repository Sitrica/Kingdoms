package com.songoda.kingdoms.command;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.manager.managers.KingdomManager;
import com.songoda.kingdoms.manager.managers.PlayerManager;
import com.songoda.kingdoms.utils.MessageBuilder;

public abstract class AbstractCommand {

	protected final FileConfiguration configuration;
	protected final KingdomManager kingdomManager;
	protected final PlayerManager playerManager;
	protected final Kingdoms instance;
	private final boolean console;
	private final String command;

	protected AbstractCommand(String command, boolean console) {
		this.console = console;
		this.command = command;
		this.instance = Kingdoms.getInstance();
		this.configuration = instance.getConfig();
		this.playerManager = instance.getManager("player", PlayerManager.class);
		this.kingdomManager = instance.getManager("kingdom", KingdomManager.class);
	}
	
	protected static void registerCommand(AbstractCommand command) {
		Kingdoms.getInstance().getCommandHandler().registerCommand(command);
	}
	
	protected enum ReturnType {
		SUCCESS,
		FAILURE,
		SYNTAX_ERROR
	}
	
	protected boolean isConsoleAllowed() {
		return console;
	}

	protected String getCommand() {
		return command;
	}

	protected abstract ReturnType runCommand(Kingdoms instance, CommandSender sender, String... arguments);

	protected abstract String getConfigurationNode();
	
	public abstract String getPermissionNode();
	
	public String getDescription(CommandSender player) {
		return new MessageBuilder("commands." + getConfigurationNode() + ".description")
				.replace("%permission%", getPermissionNode())
				.replace("%syntax%", getSyntax(player))
				.setPlaceholderObject(player)
				.get();
	}
	
	public String getSyntax(CommandSender player) {
		return new MessageBuilder("commands." + getConfigurationNode() + ".description")
				.replace("%permission%", getPermissionNode())
				.replace("%description%", getDescription(player))
				.setPlaceholderObject(player)
				.get();
	}

}
