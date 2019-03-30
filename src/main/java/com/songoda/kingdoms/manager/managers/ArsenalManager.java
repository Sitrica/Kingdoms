package com.songoda.kingdoms.manager.managers;

import com.songoda.kingdoms.manager.Manager;
import com.songoda.kingdoms.objects.kingdom.ArsenalItem;
import com.songoda.kingdoms.objects.kingdom.Kingdom;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.player.KingdomPlayer;
import com.songoda.kingdoms.objects.turrets.Turret;
import com.songoda.kingdoms.utils.DeprecationUtils;
import com.songoda.kingdoms.utils.Formatting;
import com.songoda.kingdoms.utils.HologramBuilder;
import com.songoda.kingdoms.utils.IntervalUtils;
import com.songoda.kingdoms.utils.MessageBuilder;

import java.util.List;
import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

public class ArsenalManager extends Manager {
	
	private final String ROCKET_META = "kingdoms-siege-rocket";
	private final FileConfiguration arsenal;
	private TurretManager turretManager;
	private PlayerManager playerManager;
	private WorldManager worldManager;
	private LandManager landManager;
	
	public ArsenalManager() {
		super("arsenal", true);
		this.arsenal = instance.getConfiguration("arsenal-items").get();
	}

	@Override
	public void initalize() {
		this.turretManager = instance.getManager("turret", TurretManager.class);
		this.playerManager = instance.getManager("player", PlayerManager.class);
		this.worldManager = instance.getManager("world", WorldManager.class);
		this.landManager = instance.getManager("land", LandManager.class);
	}

	@EventHandler
	public void onAttack(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (!worldManager.acceptsWorld(player.getWorld()))
			return;
		Action action = event.getAction();
		if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK)
			return;
		ItemStack itemstack = DeprecationUtils.getItemInMainHand(player);
		if (itemstack == null)
			return;
		ItemMeta meta = itemstack.getItemMeta();
		if (meta == null)
			return;
		List<String> lores = meta.getLore();
		if (meta.getLore() == null || lores.isEmpty())
			return;
		ArsenalItem type = null;
		for (ArsenalItem item : ArsenalItem.values()) {
			if (item.getMaterial() != itemstack.getType())
				continue;
			for (String lore : item.getDescription()) {
				if (lores.contains(lore)) {
					type = item;
					break;
				}
				int size = 0;
				for (String l : lores) {
					if (Formatting.stripColor(l).equalsIgnoreCase(Formatting.stripColor(lore))) {
						size++;
					}
				}
				if (size >= lores.size()) {
					type = item;
					break;
				}
			}
		}
		if (type == null)
			return;
		event.setCancelled(true);
		switch (type) {
			case SIEGE_ROCKET:
				if (itemstack.getAmount() > 0) {
					itemstack.setAmount(itemstack.getAmount() - 1);
					DeprecationUtils.setItemInMainHand(player, itemstack);
				} else {
					DeprecationUtils.setItemInMainHand(player, null);
				}
				Location playerLocation = player.getLocation();
				Location location = player.getEyeLocation().toVector().add(playerLocation.getDirection().multiply(2)).toLocation(player.getWorld(), playerLocation.getYaw(), playerLocation.getPitch());
				LargeFireball rocket = player.getWorld().spawn(location, LargeFireball.class);
				rocket.setVelocity(player.getEyeLocation().getDirection().multiply(0.5));
				rocket.setMetadata(ROCKET_META, new FixedMetadataValue(instance, player.getUniqueId()));
				rocket.setShooter(player);
				break;
			case TURRET_BREAKER:
				destroyFence(type, event);
				break;
		}
		
	}
	
	private void destroyFence(ArsenalItem type, PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Block block = event.getClickedBlock();
		if (block == null) {
			new MessageBuilder("messages.turret-breaker-wrong-usage")
					.setPlaceholderObject(block)
					.fromConfiguration(arsenal)
					.send(player);
			return;
		}
		KingdomPlayer kingdomPlayer = playerManager.getKingdomPlayer(player);
		Kingdom kingdom = kingdomPlayer.getKingdom();
		if (kingdom == null) {
			new MessageBuilder("kingdoms.no-kingdom")
					.setPlaceholderObject(player)
					.send(player);
			return;
		}
		Land land = landManager.getLand(block.getChunk());
		OfflineKingdom landKingdom = land.getKingdomOwner();
		if (landKingdom == null) {
			new MessageBuilder("messages.turret-breaker-wrong-usage")
					.setPlaceholderObject(block)
					.fromConfiguration(arsenal)
					.send(player);
			return;
		}
		if (landKingdom.equals(kingdom)) {
			new MessageBuilder("messages.turret-breaker-wrong-usage")
					.setPlaceholderObject(block)
					.fromConfiguration(arsenal)
					.send(player);
			return;
		}
		Optional<Turret> optional = turretManager.getTurret(block);
		if (!optional.isPresent()) {
			new MessageBuilder("messages.turret-breaker-wrong-usage")
					.setPlaceholderObject(block)
					.fromConfiguration(arsenal)
					.send(player);
			return;
		}
		Turret turret = optional.get();
		turret.setDisabledCooldown();
		ItemStack itemstack = DeprecationUtils.getItemInMainHand(player);
		if (itemstack.getAmount() > 0) {
			itemstack.setAmount(itemstack.getAmount() - 1);
			DeprecationUtils.setItemInMainHand(player, itemstack);
		} else {
			DeprecationUtils.setItemInMainHand(player, null);
		}
		new MessageBuilder("messages.turret-breaker-success")
				.setPlaceholderObject(block)
				.fromConfiguration(arsenal)
				.send(player);
		new HologramBuilder(turret.getLocation().add(0, 1, 0), "holograms.turret-disabled")
				.withDefaultExpiration(arsenal.getString("arsenal-items.turret-breaker.time", "2 minutes"))
				.updatableReplace("%time%", timeLeft -> IntervalUtils.getSeconds(timeLeft))
				.toPlayers(instance.getServer().getOnlinePlayers())
				.setPlaceholderObject(kingdomPlayer)
				.setKingdom(landKingdom)
				.update(true)
				.send();
	}
	
	@EventHandler
	public void rocketDestruction(EntityExplodeEvent event) {
		Entity entity = event.getEntity();
		if (!(entity instanceof LargeFireball))
			return;
		if (!entity.hasMetadata(ROCKET_META))
			return;
		event.setCancelled(true);
		Location location = entity.getLocation();
		float radius = (float) arsenal.getDouble("arsenal-items.siege-rocket.explosion-radius", 4.0);
		boolean breakBlocks = arsenal.getBoolean("arsenal-items.siege-rocket.break-blocks", false);
		boolean fire = arsenal.getBoolean("arsenal-items.siege-rocket.set-on-fire", false);
		entity.getWorld().createExplosion(location.getX(), location.getY(), location.getZ(), radius, fire, breakBlocks);
		entity.remove();
	}
	
	@Override
	public void onDisable() {}

}
