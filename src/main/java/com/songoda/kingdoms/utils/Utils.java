package com.songoda.kingdoms.utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

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
