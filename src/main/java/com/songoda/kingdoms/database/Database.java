package com.songoda.kingdoms.database;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.songoda.kingdoms.database.serializers.DefenderInfoSerializer;
import com.songoda.kingdoms.database.serializers.ItemStackSerializer;
import com.songoda.kingdoms.database.serializers.KingdomChestSerializer;
import com.songoda.kingdoms.database.serializers.LandSerializer;
import com.songoda.kingdoms.database.serializers.LocationSerializer;
import com.songoda.kingdoms.database.serializers.MiscUpgradeSerializer;
import com.songoda.kingdoms.database.serializers.OfflineKingdomPlayerSerializer;
import com.songoda.kingdoms.database.serializers.OfflineKingdomSerializer;
import com.songoda.kingdoms.database.serializers.PowerupSerializer;
import com.songoda.kingdoms.database.serializers.RankPermissionsSerializer;
import com.songoda.kingdoms.database.serializers.StructureSerializer;
import com.songoda.kingdoms.database.serializers.TurretSerializer;
import com.songoda.kingdoms.database.serializers.WarpPadSerializer;
import com.songoda.kingdoms.objects.kingdom.DefenderInfo;
import com.songoda.kingdoms.objects.kingdom.KingdomChest;
import com.songoda.kingdoms.objects.kingdom.MiscUpgrade;
import com.songoda.kingdoms.objects.kingdom.OfflineKingdom;
import com.songoda.kingdoms.objects.kingdom.Powerup;
import com.songoda.kingdoms.objects.kingdom.RankPermissions;
import com.songoda.kingdoms.objects.land.Land;
import com.songoda.kingdoms.objects.player.OfflineKingdomPlayer;
import com.songoda.kingdoms.objects.structures.Structure;
import com.songoda.kingdoms.objects.structures.WarpPad;
import com.songoda.kingdoms.objects.turrets.Turret;

public abstract class Database<T> {

	private final Gson gson;

	public Database() {
		gson = new GsonBuilder()
				.registerTypeAdapter(OfflineKingdomPlayer.class, new OfflineKingdomPlayerSerializer())
				.registerTypeAdapter(RankPermissions.class, new RankPermissionsSerializer())
				.registerTypeAdapter(OfflineKingdom.class, new OfflineKingdomSerializer())
				.registerTypeAdapter(DefenderInfo.class, new DefenderInfoSerializer())
				.registerTypeAdapter(KingdomChest.class, new KingdomChestSerializer())
				.registerTypeAdapter(MiscUpgrade.class, new MiscUpgradeSerializer())
				.registerTypeAdapter(Structure.class, new StructureSerializer())
				.registerTypeAdapter(ItemStack.class, new ItemStackSerializer())
				.registerTypeAdapter(Location.class, new LocationSerializer())
				.registerTypeAdapter(WarpPad.class, new WarpPadSerializer())
				.registerTypeAdapter(Powerup.class, new PowerupSerializer())
				.registerTypeAdapter(Turret.class, new TurretSerializer())
				.registerTypeAdapter(Land.class, new LandSerializer())
				.excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.STATIC)
				.enableComplexMapKeySerialization()
				.serializeNulls().create();
	}

	public abstract void save(String key, T value);

	public abstract T get(String key, T def);

	public abstract boolean has(String key);

	public abstract Set<String> getKeys();

	public T get(String key) {
		return get(key, null);
	}

	public void delete(String key) {
		save(key, null);
	}

	public abstract void clear();

	public String serialize(Object object, Type type) {
		return gson.toJson(object, type);
	}

	public Object deserialize(String json, Type type) {
		return gson.fromJson(json, type);
	}

}
