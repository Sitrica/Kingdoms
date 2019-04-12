package com.songoda.kingdoms.command;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.utils.Formatting;

public abstract class AbstractCommand {

	protected final FileConfiguration configuration;
	protected final Kingdoms instance;
	private final boolean console;
	private final String command;

	protected AbstractCommand(String command, boolean console) {
		this.instance = Kingdoms.getInstance();
		this.configuration = instance.getConfig();
		this.console = console;
		this.command = command;
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

	public abstract String getConfigurationNode();
	
	public abstract String getPermissionNode();
	
	public String getDescription(CommandSender sender) {
		FileConfiguration messages = instance.getConfiguration("messages").get();
		String description = messages.getString("commands." + getConfigurationNode() + ".description");
		return Formatting.color(description);
	}
	
	public String getSyntax(CommandSender sender) {
		FileConfiguration messages = instance.getConfiguration("messages").get();
		String syntax = messages.getString("commands." + getConfigurationNode() + ".syntax");
		return Formatting.color(syntax);
	}

}
