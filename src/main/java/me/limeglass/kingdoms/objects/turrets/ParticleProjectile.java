package me.limeglass.kingdoms.objects.turrets;

import java.util.Arrays;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;

import me.limeglass.kingdoms.manager.managers.external.EffectLibManager.LineBuilder;
import me.limeglass.kingdoms.utils.IntervalUtils;
import me.limeglass.kingdoms.utils.Utils;

public class ParticleProjectile {
	
	public enum ParticleMode {
		LINE, FOLLOW;
	}
	
	private final int radius, followSpeed, amount, size, timeout;
	private final String particle, color, material;
	private final ParticleMode mode;
	private final long iterations;
	
	public ParticleProjectile(ConfigurationSection section) {
		this.iterations = IntervalUtils.getInterval(section.getString("stay-time", "1 second"));
		this.followSpeed = section.getInt("follow-speed-modifier", 4);
		this.material = section.getString("material", "STONE");
		this.particle = section.getString("particle", "FLAME");
		this.timeout = section.getInt("follow-timeout", 5);
		this.color = section.getString("color", "RED");
		this.radius = section.getInt("radius", 20);
		this.amount = section.getInt("amount", 5);
		this.size = section.getInt("size", 1);
		String mode = section.getString("mode", "LINE");
		this.mode = ParticleMode.valueOf(mode.toUpperCase());
	}

	public ParticleMode getMode() {
		return mode;
	}
	
	public Material getMaterial() {
		return Utils.materialAttempt(material, "DIAMOND_BLOCK");
	}
	
	public Particle getParticle() {
		Particle particle = Particle.FLAME;
		try {
			Particle.valueOf(this.particle.toUpperCase());
		} catch (Exception e) {
			particle = Particle.FLAME;
		}
		return particle;
	}
	
	public int getFollowSpeed() {
		return followSpeed;
	}
	
	public int getIterations() {
		return (int) iterations;
	}

	public Color getColor() {
		Color color = Color.RED;
		try {
			Class<?> clazz = Color.class;
			color = (Color) Arrays.stream(clazz.getDeclaredFields())
					.parallel()
					.filter(field -> field.getName().equalsIgnoreCase(this.color))
					.map(field -> {
						try {
							return field.get(clazz);
						} catch (IllegalArgumentException | IllegalAccessException e) {
							return null;
						}
					})
					.findFirst().get();
		} catch (Exception e) {
			color = Color.RED;
		}
		return color;
	}
	
	public int getTimeout() {
		return timeout;
	}
	
	public int getAmount() {
		return amount;
	}
	
	public int getRadius() {
		return radius;
	}
	
	public int getSize() {
		return size;
	}
	
	public void shootLineParticle(LineBuilder builder, Runnable runnable) {
		builder.iterations((int) iterations)
				.withParticle(getParticle())
				.withMaterial(getMaterial())
				.withCallback(runnable)
				.withColor(getColor())
				.send(radius);
	}
	
}
