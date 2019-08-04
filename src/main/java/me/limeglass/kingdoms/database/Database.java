package me.limeglass.kingdoms.database;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import me.limeglass.kingdoms.database.serializers.DefenderInfoSerializer;
import me.limeglass.kingdoms.database.serializers.ItemStackSerializer;
import me.limeglass.kingdoms.database.serializers.LandSerializer;
import me.limeglass.kingdoms.database.serializers.LocationSerializer;
import me.limeglass.kingdoms.database.serializers.OfflineKingdomPlayerSerializer;
import me.limeglass.kingdoms.database.serializers.OfflineKingdomSerializer;
import me.limeglass.kingdoms.database.serializers.PowerupSerializer;
import me.limeglass.kingdoms.database.serializers.RankPermissionsSerializer;
import me.limeglass.kingdoms.database.serializers.StructureSerializer;
import me.limeglass.kingdoms.database.serializers.TurretSerializer;
import me.limeglass.kingdoms.database.serializers.WarpPadSerializer;
import me.limeglass.kingdoms.objects.kingdom.DefenderInfo;
import me.limeglass.kingdoms.objects.kingdom.OfflineKingdom;
import me.limeglass.kingdoms.objects.kingdom.Powerup;
import me.limeglass.kingdoms.objects.kingdom.RankPermissions;
import me.limeglass.kingdoms.objects.land.Land;
import me.limeglass.kingdoms.objects.player.OfflineKingdomPlayer;
import me.limeglass.kingdoms.objects.structures.Structure;
import me.limeglass.kingdoms.objects.structures.WarpPad;
import me.limeglass.kingdoms.objects.turrets.Turret;

public abstract class Database<T> {

	private final Gson gson;

	public Database() {
		gson = new GsonBuilder()
				.registerTypeAdapter(OfflineKingdomPlayer.class, new OfflineKingdomPlayerSerializer())
				.registerTypeAdapter(RankPermissions.class, new RankPermissionsSerializer())
				.registerTypeAdapter(OfflineKingdom.class, new OfflineKingdomSerializer())
				.registerTypeAdapter(DefenderInfo.class, new DefenderInfoSerializer())
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

	public abstract void put(String key, T value);

	public abstract T get(String key, T def);

	public abstract boolean has(String key);

	public abstract Set<String> getKeys();

	public T get(String key) {
		return get(key, null);
	}

	public void delete(String key) {
		put(key, null);
	}

	public abstract void clear();

	public String serialize(Object object, Type type) {
		return gson.toJson(object, type);
	}

	public Object deserialize(String json, Type type) {
		return gson.fromJson(json, type);
	}

}
