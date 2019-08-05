package com.songoda.kingdoms.command;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.utils.Formatting;

public abstract class AbstractCommand {

	protected final FileConfiguration configuration;
	protected final Kingdoms instance;
	private final boolean console;
	private final String[] commands;

	protected AbstractCommand(boolean console, String... commands) {
		this.instance = Kingdoms.getInstance();
		this.configuration = instance.getConfig();
		this.console = console;
		this.commands = commands;
	}

	protected enum ReturnType {
		SUCCESS,
		FAILURE,
		SYNTAX_ERROR
	}

	public boolean containsCommand(String input) {
		for (String command : commands) {
			if (command.equalsIgnoreCase(input))
				return true;
		}
		return false;
	}

	protected boolean isConsoleAllowed() {
		return console;
	}

	protected String[] getCommands() {
		return commands;
	}

	protected abstract ReturnType runCommand(Kingdoms instance, CommandSender sender, String... arguments);

	public abstract String getConfigurationNode();

	public abstract String[] getPermissionNodes();

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
