package com.songoda.kingdoms.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.manager.managers.InvadingManager;
import com.songoda.kingdoms.manager.managers.KingdomManager;
import com.songoda.kingdoms.manager.managers.LandManager;
import com.songoda.kingdoms.manager.managers.PlayerManager;
import com.songoda.kingdoms.manager.managers.external.VaultManager;
import com.songoda.kingdoms.utils.Formatting;

public abstract class AbstractCommand {

	protected final FileConfiguration configuration;
	protected final InvadingManager invadingManager;
	protected final KingdomManager kingdomManager;
	protected final PlayerManager playerManager;
	protected final VaultManager vaultManager;
	protected final LandManager landManager;
	protected final Kingdoms instance;
	private final boolean console;
	private final String command;

	protected AbstractCommand(String command, boolean console) {
		this.console = console;
		this.command = command;
		this.instance = Kingdoms.getInstance();
		this.configuration = instance.getConfig();
		this.landManager = instance.getManager("land", LandManager.class);
		this.vaultManager = instance.getManager("vault", VaultManager.class);
		this.playerManager = instance.getManager("player", PlayerManager.class);
		this.kingdomManager = instance.getManager("kingdom", KingdomManager.class);
		this.invadingManager = instance.getManager("invading", InvadingManager.class);
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
	
	public String[] getDescription(CommandSender player) {
		FileConfiguration messages = instance.getConfiguration("messages").get();
		List<String> syntaxes = new ArrayList<>();
		if (messages.isList("commands." + getConfigurationNode() + ".description"))
			syntaxes.addAll(messages.getStringList("commands." + getConfigurationNode() + ".description"));
		else
			syntaxes.add(messages.getString("commands." + getConfigurationNode() + ".description"));
		return syntaxes.parallelStream()
				.map(string -> string.replaceAll("%syntax%", Arrays.toString(getSyntax(player))))
				.map(string -> string.replaceAll("%permission%", getPermissionNode()))
				.map(string -> Formatting.color(string))
				.toArray(String[]::new);
	}
	
	public String[] getSyntax(CommandSender player) {
		FileConfiguration messages = instance.getConfiguration("messages").get();
		List<String> syntaxes = new ArrayList<>();
		if (messages.isList("commands." + getConfigurationNode() + ".syntax"))
			syntaxes.addAll(messages.getStringList("commands." + getConfigurationNode() + ".syntax"));
		else
			syntaxes.add(messages.getString("commands." + getConfigurationNode() + ".syntax"));
		return syntaxes.parallelStream()
				.map(string -> string.replaceAll("%description%", Arrays.toString(getDescription(player))))
				.map(string -> string.replaceAll("%permission%", getPermissionNode()))
				.map(string -> Formatting.color(string))
				.toArray(String[]::new);
	}

}
