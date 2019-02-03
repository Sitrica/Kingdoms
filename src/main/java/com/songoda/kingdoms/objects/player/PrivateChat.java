package com.songoda.kingdoms.objects.player;

import com.songoda.kingdoms.constants.ChatChannel;

public interface PrivateChat {
	public ChatChannel getChannel();
	public void setChannel(ChatChannel channel);
}
