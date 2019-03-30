package com.songoda.kingdoms.manager.managers;

import java.util.Set;

import com.google.common.collect.Sets;
import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.manager.managers.RankManager.Rank;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.utils.MessageBuilder;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatManager extends Manager {
	
	private PlayerManager playerManager;
	private WorldManager worldManager;
	
	public ChatManager() {
		super("chat", true);
	}
	
	@Override
	public void initalize() {
		this.playerManager = instance.getManager("player", PlayerManager.class);
		this.worldManager = instance.getManager("land", WorldManager.class);
	}
	
	public enum ChatChannel {
		KINGDOM,
		PUBLIC,
		ALLY;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChatToAlly(AsyncPlayerChatEvent event) {
		if (event.isCancelled())
			return;
		Player player = event.getPlayer();
		if (!worldManager.acceptsWorld(player.getWorld()))
			return;
		KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
		if (!kingdomPlayer.hasKingdom())
			return;
		Kingdom kingdom = kingdomPlayer.getKingdom();
		Set<KingdomPlayer> senders = Sets.newHashSet(kingdom.getOnlinePlayers());
		switch (kingdomPlayer.getChatChannel()) {
			case ALLY:
				event.setCancelled(true);
				senders.addAll(kingdom.getOnlineAllies());
				new MessageBuilder(false, "chat.ally-chat-prefix")
						.replace("%rank%", kingdomPlayer.getRank().getPrefix(kingdomPlayer))
						.setKingdom(kingdomPlayer.getKingdom())
						.replace("%player%", player.getName())
						.setPlaceholderObject(kingdomPlayer)
						.send(senders);
				break;
			case KINGDOM:
				event.setCancelled(true);
				new MessageBuilder(false, "chat.kingdom-chat-prefix")
						.replace("%rank%", kingdomPlayer.getRank().getPrefix(kingdomPlayer))
						.setKingdom(kingdomPlayer.getKingdom())
						.replace("%player%", player.getName())
						.setPlaceholderObject(kingdomPlayer)
						.send(senders);
				break;
			case PUBLIC:
				Rank rank = kingdomPlayer.getRank();
				FileConfiguration ranks = instance.getConfiguration("ranks").get();
				if (ranks.getBoolean("ranks-use-prefix", true))
					event.setFormat(rank.getPrefix(kingdomPlayer) + event.getFormat());
				if (ranks.getBoolean("ranks-use-chat-color", true))
					event.setMessage(rank.getChat() + event.getMessage());
				break;
		}
	}

	@Override
	public void onDisable() {}

}
