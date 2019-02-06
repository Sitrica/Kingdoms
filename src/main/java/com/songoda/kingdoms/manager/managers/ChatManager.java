package com.songoda.kingdoms.manager.managers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.manager.managers.RankManager.Rank;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.utils.MessageBuilder;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;

public class ChatManager extends Manager {
	
	static {
		registerManager("chat", new ChatManager());
	}
	
	private final WorldManager worldManager;
	
	protected ChatManager() {
		super(true);
		this.worldManager = instance.getManager("land", WorldManager.class);
	}
	
	public enum ChatChannel {
		KINGDOM,
		PUBLIC,
		ALLY;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChat(AsyncPlayerChatEvent event) {
		if (event.isCancelled())
			return;
		Player player = event.getPlayer();
		if (!worldManager.acceptsWorld(player.getWorld()))
			return;
		KingdomPlayer kingdomPlayer = GameManagement.getPlayerManager().getSession(player);
		if (!kingdomPlayer.hasKingdom())
			return;
		if (kingdomPlayer.getChatChannel() != ChatChannel.PUBLIC)
			return;
		Rank rank = kingdomPlayer.getRank();
		if (configuration.getBoolean("ranks-use-prefix", true))
			event.setFormat(rank.getPrefix(kingdomPlayer) + event.getFormat());
		if (configuration.getBoolean("ranks-use-chat-color", true))
			event.setMessage(rank.getChat() + event.getMessage());
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onChatToAlly(AsyncPlayerChatEvent event) {
		if (event.isCancelled())
			return;
		Player player = event.getPlayer();
		if (!worldManager.acceptsWorld(player.getWorld()))
			return;
		KingdomPlayer kingdomPlayer = GameManagement.getPlayerManager().getSession(player);
		if (!kingdomPlayer.hasKingdom())
			return;
		if (kingdomPlayer.getChatChannel() != ChatChannel.ALLY)
			return;
		event.setCancelled(true);
		new MessageBuilder(false, "chat.ally-chat-prefix")
				.replace("%rank%", kingdomPlayer.getRank().getPrefix(kingdomPlayer))
				.replace("%player%", player.getName())
				.send(kingdomPlayer.getKingdom().getA);// TODO aliases, left off here
		
		String tag = Kingdoms.getLang().getString("Chat_Allychat_Prefix");
		//e.setFormat(color + prefix + kp.getName() + ": " + e.getMessage());
		sendMessageToKingdomAllies(tag.replaceAll("%player%", kp.getName()).replaceAll("%rank%", kp.getRank().getFancyMark()).replaceAll("%kingdom%", kp.getKingdomName()) + e.getMessage(), kp.getKingdom());
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onChatToKingdom(AsyncPlayerChatEvent e) {
		if(e.isCancelled()) return;
		if(!Config.getConfig().getStringList("enabled-worlds").contains(e.getPlayer().getWorld().getName())){
			return;
		}
		KingdomPlayer kp = GameManagement.getPlayerManager().getSession(e.getPlayer());
		if(kp == null)
			return;
		if (kp.getKingdom() == null)
			return;
		
		if(kp.getChannel() != ChatChannel.KINGDOM)
			return;

		String prefix = "";
		String tag = Kingdoms.getLang().getString("Chat_Kingdomchat_Prefix");
		if(Config.getConfig().getBoolean("useKingdomPrefixes")){
			String message = ChatColor.GRAY + e.getMessage();
			
			prefix += ChatColor.stripColor(getFancyBracket(kp));

		}

		//e.setFormat();
		//e.setMessage(ChatColor.GREEN + e.getMessage());
		e.setCancelled(true);
		sendMessageToKingdomPlayers(tag.replaceAll("%player%", kp.getName()).replaceAll("%rank%", kp.getRank().getFancyMark()).replaceAll("%kingdom%", kp.getKingdomName()) + e.getMessage(), kp.getKingdom());
	}

	@Override
	public void onDisable() {}

}
