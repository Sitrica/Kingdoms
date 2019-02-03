package com.songoda.kingdoms.objects.kingdom;

import java.util.HashMap;
import java.util.Map;

public class KingdomCooldown {
 
    private static Map<String, KingdomCooldown> cooldowns = new HashMap<String, KingdomCooldown>();
    private long start;
    private final int timeInSeconds;
    private final String kingdomName;
    private final String cooldownName;
 
    public KingdomCooldown(String kingdomName, String cooldownName, int timeInSeconds){
        this.kingdomName = kingdomName;
        this.cooldownName = cooldownName;
        this.timeInSeconds = timeInSeconds;
    }
 
    public static boolean isInCooldown(String id, String cooldownName){
        if(getTimeLeft(id, cooldownName)>=1){
            return true;
        } else {
            stop(id, cooldownName);
            return false;
        }
    }
 
    private static void stop(String id, String cooldownName){
        KingdomCooldown.cooldowns.remove(id+cooldownName);
    }
 
    private static KingdomCooldown getCooldown(String id, String cooldownName){
        return cooldowns.get(id.toString()+cooldownName);
    }
 
    public static int getTimeLeft(String id, String cooldownName){
        KingdomCooldown cooldown = getCooldown(id, cooldownName);
        int f = -1;
        if(cooldown!=null){
            long now = System.currentTimeMillis();
            long cooldownTime = cooldown.start;
            int totalTime = cooldown.timeInSeconds;
            int r = (int) (now - cooldownTime) / 1000;
            f = (r - totalTime) * (-1);
        }
        
        
        return f;
    }
 
    public void start(){
        this.start = System.currentTimeMillis();
        cooldowns.put(this.kingdomName.toString()+this.cooldownName, this);
    }
 
}
 
