package com.songoda.kingdoms.objects.kingdom;

import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.objects.player.OfflineKingdomPlayer;

public interface KingdomEventHandler {
	public void onOtherKingdomLoad(Kingdom k);
	public void onOtherKingdomUnLoad(Kingdom k);
	public void onKingdomPlayerLogin(KingdomPlayer kp);
	public void onKingdomPlayerLogout(KingdomPlayer kp);
	public void onMemberJoinKingdom(OfflineKingdomPlayer kp);
	public void onMemberQuitKingdom(OfflineKingdomPlayer kp);
	public void onKingdomDelete(Kingdom k);
}
