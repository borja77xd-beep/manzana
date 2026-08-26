package com.siko.sikologinparticles;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Configuracion muy simple del mod, guardada en JSON dentro de la carpeta
 * "config" de Fabric Loader: config/sikologinparticles.json
 */
public class ModConfig {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final String FILE_NAME = "sikologinparticles.json";

	/** Activa o desactiva por completo el mod. */
	public boolean enabled = true;

	/** Ticks a esperar tras entrar al mundo antes de reproducir la animacion. */
	public int delayTicks = 5;

	/** Duracion total de la animacion en ticks (40 ticks = 2 segundos). */
	public int durationTicks = 40;

	/** Radio de la espiral, en bloques. */
	public double radius = 0.7;

	/** Numero total de particulas a repartir a lo largo de toda la animacion. */
	public int particleCount = 50;

	/**
	 * Identificador de la particula vanilla a usar (debe ser una particula
	 * "simple", sin parametros extra, por ejemplo: minecraft:end_rod,
	 * minecraft:enchant, minecraft:happy_villager, minecraft:crit...
	 * Si el identificador no es valido o no es una particula simple, se usara
	 * minecraft:end_rod como respaldo.
	 */
	public String particleType = "minecraft:end_rod";

	/** Si se reproduce un sonido corto junto con la animacion. */
	public boolean playSound = true;

	/** Volumen del sonido (0.0 - 1.0 recomendado). */
	public float soundVolume = 0.4f;

	/**
	 * Identificador del sonido vanilla a reproducir.
	 * Por defecto un "chime" suave y poco intrusivo.
	 */
	public String soundId = "minecraft:block.amethyst_block.chime";

	/**
	 * Carga la configuracion desde disco, creando el archivo con los valores
	 * por defecto si no existe todavia.
	 */
	public static ModConfig load() {
		Path path = configPath();
		ModConfig config = new ModConfig();

		if (Files.exists(path)) {
			try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
				ModConfig loaded = GSON.fromJson(reader, ModConfig.class);
				if (loaded != null) {
					config = loaded;
				}
			} catch (IOException | RuntimeException e) {
				SikoLoginParticlesClient.LOGGER.warn(
						"[SikoLoginParticles] No se pudo leer la configuracion, se usaran valores por defecto.", e);
			}
		}

		// Siempre se vuelve a guardar: crea el archivo si no existia y
		// completa campos nuevos si el usuario tenia una version antigua.
		config.save();
		return config;
	}

	/** Guarda la configuracion actual en disco. */
	public void save() {
		Path path = configPath();
		try {
			Files.createDirectories(path.getParent());
			try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
				GSON.toJson(this, writer);
			}
		} catch (IOException e) {
			SikoLoginParticlesClient.LOGGER.warn("[SikoLoginParticles] No se pudo guardar la configuracion.", e);
		}
	}

	private static Path configPath() {
		return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
	}

	/**
	 * Resuelve el identificador configurado a una particula vanilla "simple".
	 * Si no es valida, cae de vuelta a END_ROD.
	 */
	public ParticleEffect resolveParticle() {
		try {
			Identifier id = Identifier.of(particleType);
			ParticleType<?> type = Registries.PARTICLE_TYPE.get(id);
			if (type instanceof ParticleEffect effect) {
				return effect;
			}
		} catch (RuntimeException e) {
			SikoLoginParticlesClient.LOGGER.warn(
					"[SikoLoginParticles] particle_type '{}' invalido, usando end_rod.", particleType);
		}
		return ParticleTypes.END_ROD;
	}

	/**
	 * Resuelve el sonido configurado. Si no es valido, cae de vuelta a un
	 * sonido vanilla suave por defecto.
	 */
	public SoundEvent resolveSound() {
		try {
			Identifier id = Identifier.of(soundId);
			SoundEvent event = Registries.SOUND_EVENT.get(id);
			if (event != null) {
				return event;
			}
		} catch (RuntimeException e) {
			SikoLoginParticlesClient.LOGGER.warn(
					"[SikoLoginParticles] sound_id '{}' invalido, usando sonido por defecto.", soundId);
		}
		return SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME;
	}
}
