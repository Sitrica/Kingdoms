package com.songoda.kingdoms.objects.turrets;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;

import com.songoda.kingdoms.Kingdoms;
import com.songoda.kingdoms.manager.managers.TurretManager;
import com.songoda.kingdoms.utils.Utils;

public class Turret {

	private final Set<LivingEntity> following = new HashSet<>();
	private long fire, reload, ammo, disabledCooldown;
	private final Location location;
	private final TurretType type;
	private boolean disabled;

	public Turret(Location location, TurretType type) {
		this.disabledCooldown = System.currentTimeMillis();
		this.reload = System.currentTimeMillis();
		this.fire = System.currentTimeMillis();
		this.ammo = type.getAmmo();
		this.location = location;
		this.type = type;
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

	public Location getHeadLocation() {
		return location;
	}

	public Location getPostLocation() {
		return location.clone().subtract(0, 1, 0);
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

	public long getDisabledCooldown() {
		return disabledCooldown;
	}

	public void setDisabledCooldown() {
		this.disabledCooldown = System.currentTimeMillis();
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void resetAmmo() {
		ammo = type.getAmmo();
	}

	public long getAmmo() {
		return ammo;
	}

	public void useAmmo() {
		ammo = ammo - 1;
	}

	public boolean isValid() {
		if (type == null)
			return false;
		if (location == null)
			return false;
		Block block = location.getBlock();
		if (!block.getRelative(0, -1, 0).getType().toString().endsWith("FENCE"))
			return false;
		Material check = Utils.materialAttempt("PLAYER_HEAD", "SKULL");
		return block.getType() == check;
	}

	public void fireAt(LivingEntity target) {
		if (disabled)
			return;
		Kingdoms.getInstance().getManager(TurretManager.class).fire(this, target);
	}

}
