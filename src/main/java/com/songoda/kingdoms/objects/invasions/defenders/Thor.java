package com.songoda.kingdoms.objects.invasions.defenders;

import java.lang.reflect.Constructor;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.events.DefenderDamageByPlayerEvent;
import com.songoda.kingdoms.events.DefenderDamagePlayerEvent;
import com.songoda.kingdoms.events.DefenderThorEvent;
import com.songoda.kingdoms.manager.managers.PlayerManager;
import com.songoda.kingdoms.objects.DefenderAbility;
import com.songoda.kingdoms.objects.invasions.Invasion;
import com.songoda.kingdoms.objects.kingdom.DefenderInfo;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.utils.MessageBuilder;
import com.songoda.kingdoms.utils.Utils;

public class Thor extends DefenderAbility {

	private final Class<?> weatherPacket = getNMSClass("PacketPlayOutSpawnEntityWeather");
	private final Class<?> lightningClass = getNMSClass("EntityLightning");
	private final Class<?> entityClass = getNMSClass("Entity");
	private final Class<?> worldClass = getNMSClass("World");

	public Thor() {
		super(false, "thor");
	}

	@Override
	public boolean initialize(Kingdoms instance) {
		return true;
	}

	@Override
	public void tick(LivingEntity defender, DefenderInfo info, Invasion invasion) {
		int thor = info.getThor();
		KingdomPlayer instigator = invasion.getInstigator();
		Player player = instigator.getPlayer();
		if (section.getBoolean("attack-invasion-instigator", true)) {
			if (!player.isDead() && player.isOnline()) {
				sendLightning(player, player.getLocation());
				player.damage(thor * section.getDouble("instigator-damage-bonus", 1.0), defender);
				new MessageBuilder("defenders.defender-thor")
						.setPlaceholderObject(instigator)
						.send(player);
			}
		}
		if (section.getBoolean("attack-in-radius", true)) {
			int radius = section.getInt("attack-radius", 8);
			UUID playerUUID = player.getUniqueId();
			PlayerManager playerManager = Kingdoms.getInstance().getManager(PlayerManager.class);
			for (Entity entity : defender.getNearbyEntities(radius, radius, radius)) {
				if (!(entity instanceof Player))
					continue;
				if (entity.getUniqueId().equals(playerUUID))
					continue;
				Player p = (Player) entity;
				KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(p);
				Kingdom kingdom = kingdomPlayer.getKingdom();
				if (kingdom == null || (!kingdom.equals(kingdom) && !kingdom.isAllianceWith(kingdom))) {
					DefenderThorEvent thorEvent = new DefenderThorEvent(kingdom, defender, kingdomPlayer);
					Bukkit.getPluginManager().callEvent(thorEvent);
					if (thorEvent.isCancelled())
						return;
					sendLightning(p, p.getLocation());
					p.damage(thorEvent.getDamage() * section.getDouble("radius-damage-bonus", 1.0), defender);
					new MessageBuilder("defenders.defender-thor")
							.setPlaceholderObject(kingdomPlayer)
							.setKingdom(kingdom)
							.send(p);
				}
			}
		}
	}

	public void sendLightning(Player player, Location location) {
		try {
			Constructor<?> constructor = lightningClass.getConstructor(worldClass, double.class, double.class, double.class, boolean.class, boolean.class);
			Object world = player.getWorld().getClass().getMethod("getHandle").invoke(player.getWorld());
			Object lightning = constructor.newInstance(world, location.getX(), location.getY(), location.getZ(), false, false);
			Object object = weatherPacket.getConstructor(entityClass).newInstance(lightning);
			sendPacket(player, object);
			Sound sound = Utils.soundAttempt("ENTITY_LIGHTNING_BOLT_THUNDER", "AMBIENCE_THUNDER");
			if (sound == null) //1.9-1.12 users...
				sound = Utils.soundAttempt("ENTITY_LIGHTNING_THUNDER", "LIGHTNING_THUNDER");
			player.playSound(player.getLocation(), sound, 100, 1);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public Class<?> getNMSClass(String name) {
		String version = Kingdoms.getInstance().getServer().getClass().getPackage().getName().split("\\.")[3];
		try {
			return Class.forName("net.minecraft.server." + version + "." + name);
		} catch(ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void sendPacket(Player player, Object packet) {
		try{
			Object handle = player.getClass().getMethod("getHandle").invoke(player);
			Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
			playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void onDamaging(DefenderDamagePlayerEvent event) {}

	@Override
	public void onDamage(DefenderDamageByPlayerEvent event) {}

	@Override
	public void onDeath(LivingEntity defender) {}

	@Override
	public void onAdd(LivingEntity defender) {}

}
