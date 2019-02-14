package com.songoda.kingdoms.utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TropicalFish.Pattern;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.inventory.meta.TropicalFishBucketMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import com.songoda.kingdoms.Kingdoms;

public class Utils {

	private static JarFile getJar(Kingdoms instance) {
		try {
			Method method = JavaPlugin.class.getDeclaredMethod("getFile");
			method.setAccessible(true);
			File file = (File) method.invoke(instance);
			return new JarFile(file);
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@SuppressWarnings("deprecation")
	public static OfflinePlayer getSkullOwner(String input) {
		if (isUUID(input))
			return Bukkit.getOfflinePlayer(getUniqueId(input));
		else
			return Bukkit.getOfflinePlayer(input);
	}
	
	public static boolean isUUID(String uuid) {
		try {
			UUID.fromString(uuid);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public static UUID getUniqueId(String uuid) {
		try {
			return UUID.fromString(uuid);
		} catch (Exception e) {
			return null;
		}
	}
	
	@SuppressWarnings("deprecation")
	public static ItemStack setupItemMeta(ItemStack itemstack, String meta) {
		ItemMeta itemMeta = itemstack.getItemMeta();
		if (itemMeta instanceof PotionMeta) {
			PotionMeta potionMeta = (PotionMeta) itemMeta;
			PotionType type;
			try {
				type = PotionType.valueOf(meta);
			} catch (Exception e) {
				type = PotionType.SPEED;
			}
			potionMeta.setBasePotionData(new PotionData(type));
			itemstack.setItemMeta(potionMeta);
		}
		if (itemMeta instanceof SkullMeta) {
			SkullMeta skullMeta = (SkullMeta) itemMeta;
			skullMeta.setOwningPlayer(getSkullOwner(meta));
			itemstack.setItemMeta(skullMeta);
		}
		if (itemMeta instanceof TropicalFishBucketMeta) {
			TropicalFishBucketMeta fishMeta = (TropicalFishBucketMeta) itemMeta;
			String[] metas = meta.split(":");
			if (metas.length < 2)
				return itemstack;
			Pattern pattern;
			try {
				pattern = Pattern.valueOf(metas[1]);
			} catch (Exception e) {
				pattern = Pattern.BETTY;
			}
			fishMeta.setPattern(pattern);
			DyeColor color;
			try {
				color = DyeColor.valueOf(metas[0]);
			} catch (Exception e) {
				color = DyeColor.GREEN;
			}
			fishMeta.setBodyColor(color);
			itemstack.setItemMeta(fishMeta);
		}
		if (itemMeta instanceof SpawnEggMeta) {
			SpawnEggMeta eggMeta = (SpawnEggMeta) itemMeta;
			EntityType entity;
			try {
				entity = EntityType.valueOf(meta);
			} catch (Exception e) {
				entity = EntityType.ZOMBIE;
			}
			eggMeta.setSpawnedType(entity);
			itemstack.setItemMeta(eggMeta);
		}
		if (itemMeta instanceof LeatherArmorMeta) {
			LeatherArmorMeta leatherMeta = (LeatherArmorMeta) itemMeta;
			Color color;
			String[] colors = meta.split(":");
			if (colors.length < 3)
				return itemstack;
			int r = Integer.parseInt(colors[0]);
			int g = Integer.parseInt(colors[1]);
			int b = Integer.parseInt(colors[2]);
			try {
				color = Color.fromBGR(r, g, b);
			} catch (Exception e) {
				color = Color.RED;
			}
			leatherMeta.setColor(color);
			itemstack.setItemMeta(leatherMeta);
		}
		if (itemMeta instanceof BannerMeta) {
			BannerMeta bannerMeta = (BannerMeta) itemMeta;
			DyeColor color;
			try {
				color = DyeColor.valueOf(meta);
			} catch (Exception e) {
				color = DyeColor.RED;
			}
			bannerMeta.setBaseColor(color);
			itemstack.setItemMeta(bannerMeta);
		}
		return itemstack;
	}
	
	public static void loadClasses(Kingdoms instance, String basePackage, String... subPackages) {
		for (int i = 0; i < subPackages.length; i++) {
			subPackages[i] = subPackages[i].replace('.', '/') + "/";
		}
		JarFile jar = getJar(instance);
		if (jar == null)
			return;
		basePackage = basePackage.replace('.', '/') + "/";
		try {
			for (Enumeration<JarEntry> jarEntry = jar.entries(); jarEntry.hasMoreElements();) {
				String name = jarEntry.nextElement().getName();
				if (name.startsWith(basePackage) && name.endsWith(".class")) {
					for (String sub : subPackages) {
						if (name.startsWith(sub, basePackage.length())) {
							String clazz = name.replace("/", ".").substring(0, name.length() - 6);
							Class.forName(clazz, true, instance.getClass().getClassLoader());
						}
					}
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				jar.close();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> Set<Class<T>> getClassesOf(Kingdoms instance, String basePackage, Class<T> type) {
		JarFile jar = getJar(instance);
		if (jar == null)
			return null;
		basePackage = basePackage.replace('.', '/') + "/";
		Set<Class<T>> classes = new HashSet<>();
		try {
			for (Enumeration<JarEntry> jarEntry = jar.entries(); jarEntry.hasMoreElements();) {
				String name = jarEntry.nextElement().getName();
				if (name.startsWith(basePackage) && name.endsWith(".class")) {
					String className = name.replace("/", ".").substring(0, name.length() - 6);
					Class<?> clazz = Class.forName(className, true, instance.getClass().getClassLoader());
					if (type.isAssignableFrom(clazz))
						classes.add((Class<T>) clazz);
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				jar.close();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
		return classes;
	}
	
	/**
	 * Tests whether a given class exists in the classpath.
	 * 
	 * @author Skript team.
	 * @param className The {@link Class#getCanonicalName() canonical name} of the class
	 * @return Whether the given class exists.
	 */
	public static boolean classExists(String className) {
		try {
			Class.forName(className);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	/**
	 * Tests whether a method exists in the given class.
	 * 
	 * @author Skript team.
	 * @param c The class
	 * @param methodName The name of the method
	 * @param parameterTypes The parameter types of the method
	 * @return Whether the given method exists.
	 */
	public static boolean methodExists(Class<?> c, String methodName, Class<?>... parameterTypes) {
		try {
			c.getDeclaredMethod(methodName, parameterTypes);
			return true;
		} catch (NoSuchMethodException | SecurityException e) {
			return false;
		}
	}
	
	public static EntityType entityAttempt(String attempt, String fallback) {
		EntityType entity = null;
		try {
			entity = EntityType.valueOf(attempt);
		} catch (Exception e) {
			try {
				entity = EntityType.valueOf(fallback);
			} catch (Exception e1) {}
		}
		if (entity == null)
			entity = EntityType.ARROW;
		return entity;
	}
	
	public static Material materialAttempt(String attempt, String fallback) {
		Material material = null;
		try {
			material = Material.valueOf(attempt);
		} catch (Exception e) {
			try {
				material = Material.valueOf(fallback);
			} catch (Exception e1) {}
		}
		if (material == null)
			material = Material.CHEST;
		return material;
	}
	
}
