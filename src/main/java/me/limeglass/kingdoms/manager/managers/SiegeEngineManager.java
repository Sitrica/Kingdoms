package me.limeglass.kingdoms.manager.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import me.limeglass.kingdoms.manager.Manager;
import me.limeglass.kingdoms.objects.kingdom.Kingdom;
import me.limeglass.kingdoms.objects.kingdom.OfflineKingdom;
import me.limeglass.kingdoms.objects.land.Land;
import me.limeglass.kingdoms.objects.structures.SiegeEngine;
import me.limeglass.kingdoms.utils.MessageBuilder;

public class SiegeEngineManager extends Manager {
	
	private final Map<Integer, Integer> crucial = new HashMap<>();
	private final FileConfiguration structures;
	
	public SiegeEngineManager() {
		super("siege-engine", false);
		crucial.put(4, 4);
		crucial.put(4, 8);
		crucial.put(4, 12);
		crucial.put(8, 4);
		crucial.put(8, 8);
		crucial.put(8, 12);
		crucial.put(12, 4);
		crucial.put(12, 8);
		crucial.put(12, 12);
		structures = instance.getConfiguration("structures").get();
	}
	
	public void fireSiegeEngine(SiegeEngine engine, Land land, Kingdom kingdom, OfflineKingdom target) {
		engine.resetCooldown();
		/*
		boolean isTargetShielded;
		if (target.isWithinNexusShieldRange(land)) {
			if (target.getShieldValue() > 0)
				isTargetShielded = true;
		}
		*/
		boolean messageSent = false;
		Chunk chunk = land.getChunk();
		World world = chunk.getWorld();
		for (Entry<Integer,Integer> crucial : crucial.entrySet()) {
			int x = crucial.getKey();
			int z = crucial.getValue();
			Location location = chunk.getBlock(x, 0, z).getLocation();
			int y = world.getHighestBlockYAt(location);
//			if (isTargetShielded)
//				y += 10;
			location = chunk.getBlock(x, y, z).getLocation();
			/*if (isTargetShielded) {
				target.getWorld().playSound(boom, Sounds.EXPLODE.bukkitSound(), 3.0f, 1.0f);
				if(!messageSent){
					targetKingdom.setShieldValue(targetKingdom.getShieldValue()- Config.getConfig().getInt("siege.fire.shield-damage"));
					targetKingdom.sendAnnouncement(null, 
							Kingdoms.getLang().getString("Siege_Warning_Shielded")
							.replaceAll("%value%",""+targetKingdom.getShieldValue())
							.replaceAll("%max%",""+targetKingdom.getShieldMax())
							.replaceAll("%kingdom%",""+firingKingdom.getKingdomName()),
							true);
					firingKingdom.sendAnnouncement(null, 
							Kingdoms.getLang().getString("Siege_Success_Shielded")
							.replaceAll("%value%",""+targetKingdom.getShieldValue())
							.replaceAll("%max%",""+targetKingdom.getShieldMax())
							.replaceAll("%kingdom%",""+targetKingdom.getKingdomName()),
							true);
					messageSent = true;
				}
			}else{
			*/
			world.createExplosion(location, (float) structures.getDouble("structures.siege-engine.explosion-radius"));
			if (!messageSent) {
				if (target.isOnline()) {
					Kingdom targetKingdom = target.getKingdom();
					new MessageBuilder("structures.siege-warning")
							.toKingdomPlayers(targetKingdom.getOnlinePlayers())
							.replace("%kingdom%", kingdom.getName())
							.setKingdom(targetKingdom)
							.send();
				}
				new MessageBuilder("structures.siege-warning")
						.toKingdomPlayers(kingdom.getOnlinePlayers())
						.replace("%kingdom%", target.getName())
						.setKingdom(kingdom)
						.send();
				messageSent = true;
			}
		}
	}

	@Override
	public void initalize() {}

	@Override
	public void onDisable() {
		crucial.clear();
	}

}
