package com.songoda.kingdoms.objects.land;

import com.songoda.kingdoms.constants.StructureType;
import com.songoda.kingdoms.main.Config;

public class SiegeEngine extends Structure {
	
	public long fireCooldown;
	
	
	public SiegeEngine(SimpleLocation loc, StructureType type) {
		super(loc, type);
		fireCooldown = System.currentTimeMillis();
	}
	
	public SiegeEngine(SimpleLocation loc, StructureType type, long concBlastMillis) {
		super(loc, type);
		this.fireCooldown = concBlastMillis;
	}
	
	public void readyFire(){
		fireCooldown = System.currentTimeMillis();
	}
	
	public void resetFireCooldown(){
		fireCooldown = System.currentTimeMillis();
	}
	
	public boolean isReadyToFire(){
		return getConcBlastCD() <= 0;
	}
	
	
    public int getConcBlastCD(){
        int f = -1;
            long now = System.currentTimeMillis();
            int totalTime = Config.getConfig().getInt("siege.fire.cooldown")*60;
            int r = (int) (now - fireCooldown) / 1000;
            f = (r - totalTime) * (-1);
            
        return f/60;
    }

}
