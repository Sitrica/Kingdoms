package com.songoda.kingdoms.objects.land;

import com.songoda.kingdoms.constants.StructureType;
import com.songoda.kingdoms.main.Config;
import com.songoda.kingdoms.main.Kingdoms;
import com.songoda.kingdoms.manager.game.GameManagement;
import com.songoda.kingdoms.objects.kingdom.Kingdom;

public class Extractor extends Structure {
	
	private long timeToNextCollection;

	public Extractor(SimpleLocation loc, StructureType type) {
		super(loc, type);
		timeToNextCollection = System.currentTimeMillis();
	}
	
	public Extractor(SimpleLocation loc, StructureType type, long timeToNextCollection) {
		super(loc, type);
		this.timeToNextCollection = timeToNextCollection;
	}
	
	public void collect(){
		if(!isReady()) return;
		timeToNextCollection = System.currentTimeMillis();
		Land land = GameManagement.getLandManager().getOrLoadLand(this.getLoc().toSimpleChunk());
		if(land.getOwnerUUID() != null){
			Kingdom kingdom = GameManagement.getKingdomManager().getOrLoadKingdom(land.getOwnerUUID());
			kingdom.setResourcepoints(kingdom.getResourcepoints() + Config.getConfig().getInt("mine.reward-amount"));
			kingdom.sendAnnouncement(null, Kingdoms.getLang().getString("Extractor_Collection_Announcement").replaceAll("%amount%", "" + Config.getConfig().getInt("mine.reward-amount")), true);
		}
	}
	public void resetTime(){
		timeToNextCollection = System.currentTimeMillis();
	}
	
	public boolean isReady(){
		return getTimeLeft() <= 0;
	}
	
	public long getTimeToNextCollection(){
		return timeToNextCollection;
	}
	
	
    public int getTimeLeft(){
        int f;
            long now = System.currentTimeMillis();
            int totalTime = Config.getConfig().getInt("mine.reward-delay-in-minutes")*60;
            int r = (int) (now - timeToNextCollection) / 1000;
            f = (r - totalTime) * (-1);
            
        return f/60;
    }

}
