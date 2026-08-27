package com.siko.sikologinparticles;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
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
 * Configuracion general del mod (server-side), guardada en JSON en
 * config/sikologinparticles.json. No incluye "particle_type": cada efecto
 * (hojas / sakura / viento) usa sus propias particulas vanilla fijas; lo que
 * es configurable aqui es el timing, el radio y el sonido, aplicados por
 * igual a los 3 efectos.
 */
public class ModConfig {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final String FILE_NAME = "sikologinparticles.json";

	/** Interruptor general: si esta en false, no se dispara ningun efecto. */
	public boolean enabled = true;

	/** Ticks a esperar tras entrar al mundo antes de reproducir el efecto. */
	public int delayTicks = 5;

	/** Duracion total de la animacion en ticks (40 ticks = 2 segundos). */
	public int durationTicks = 40;

	/** Radio aproximado, en bloques, del efecto alrededor del jugador. */
	public double radius = 0.7;

	/** Numero total de particulas a repartir a lo largo de toda la animacion. */
	public int particleCount = 50;

	/** Si se reproduce un sonido corto junto con la animacion. */
	public boolean playSound = true;

	/** Volumen del sonido (0.0 - 1.0 recomendado). */
	public float soundVolume = 0.4f;

	/** Identificador del sonido vanilla a reproducir al empezar el efecto. */
	public String soundId = "minecraft:block.amethyst_block.chime";

	/** Carga la configuracion desde disco, creandola con valores por defecto si no existe. */
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
				SikoLoginParticlesServer.LOGGER.warn(
						"[SikoLoginParticles] No se pudo leer la configuracion, se usaran valores por defecto.", e);
			}
		}

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
			SikoLoginParticlesServer.LOGGER.warn("[SikoLoginParticles] No se pudo guardar la configuracion.", e);
		}
	}

	private static Path configPath() {
		return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
	}

	/** Resuelve el sonido configurado, con un valor vanilla de respaldo si no es valido. */
	public SoundEvent resolveSound() {
		try {
			Identifier id = Identifier.of(soundId);
			SoundEvent event = Registries.SOUND_EVENT.get(id);
			if (event != null) {
				return event;
			}
		} catch (RuntimeException e) {
			SikoLoginParticlesServer.LOGGER.warn(
					"[SikoLoginParticles] sound_id '{}' invalido, usando sonido por defecto.", soundId);
		}
		return SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME;
	}
}
