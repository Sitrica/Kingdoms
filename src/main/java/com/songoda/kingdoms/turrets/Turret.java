package com.songoda.kingdoms.turrets;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.manager.managers.TurretManager;
import com.songoda.kingdoms.utils.Utils;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class Turret {

	private final Set<LivingEntity> following = new HashSet<>();
	private final TurretManager turretManager;
	private long fire, reload, currentAmmo;
	private final Location location;
	private final TurretType type;
	private final boolean post;
	private final long ammo;

	public Turret(Location location, TurretType type) {
		this(location, type, false);
	}
	
	public Turret(Location location, TurretType type, boolean post) {
		this.turretManager = Kingdoms.getInstance().getManager("turret", TurretManager.class);
		this.reload = System.currentTimeMillis();
		this.fire = System.currentTimeMillis();
		this.ammo = type.getAmmo();
		this.location = location;
		this.type = type;
		this.post = post;
	}
	
	/**
	 * If this is set to true, then a fence or cobblestone wall by default was generated
	 * It's generated when the user clicks the ground and there was no fence post to begin with.
	 * 
	 * @return
	 */
	public boolean hasCreatedPost() {
		return post;
	}
	
	public Set<LivingEntity> getFollowing() {
		return following;
	}
	
	public void addFollowing(LivingEntity entity) {
		following.add(entity);
	}
	
	public void removeFollowing(LivingEntity entity) {
		following.remove(entity);
	}
	
	public boolean isFollowing(LivingEntity entity) {
		return following.contains(entity);
	}
	
	public Location getLocation() {
		return location;
	}
	
	public TurretType getType() {
		return type;
	}

	public long getFireCooldown() {
		return fire;
	}

	public void setFireCooldown() {
		this.fire = System.currentTimeMillis();
	}
	
	public long getReloadCooldown() {
		return reload;
	}

	public void setReloadCooldown() {
		this.reload = System.currentTimeMillis();
	}
	
	public void resetAmmo() {
		this.currentAmmo = ammo;
	}
	
	public long getAmmo() {
		return currentAmmo;
	}
	
	public void useAmmo() {
		this.currentAmmo += 1;
	}

	public boolean isValid() {
		if (type == null)
			return false;
		if (location == null)
			return false;
		Block block = location.getBlock();
		if (!block.getRelative(0, -1, 0).getType().toString().endsWith("FENCE"))
			return false;
		Material check = Utils.materialAttempt("SKELETON_SKULL", "SKULL");
		return block.getType() == check;
	}
	
	public void fireAt(Player target) {
		turretManager.fire(this, target);
	}

}
