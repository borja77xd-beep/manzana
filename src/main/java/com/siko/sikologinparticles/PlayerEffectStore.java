package com.siko.sikologinparticles;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Guarda, por nombre de jugador, que efecto de login tiene asignado (o
 * ninguno). Se guarda por nombre (en minusculas) en vez de por UUID para
 * mantenerlo simple: un administrador puede asignar el efecto a un jugador
 * aunque este nunca haya entrado antes o este desconectado en ese momento.
 * <p>
 * Limitacion conocida: si un jugador cambia de nombre de usuario, habria que
 * volver a asignarle el efecto con el nombre nuevo.
 */
public class PlayerEffectStore {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final String FILE_NAME = "sikologinparticles_players.json";
	private static final Type MAP_TYPE = new TypeToken<HashMap<String, String>>() {}.getType();

	/** nombre de jugador (minusculas) -> id del efecto (ver SikoEffectType). */
	private final Map<String, String> assignments;

	private PlayerEffectStore(Map<String, String> assignments) {
		this.assignments = assignments;
	}

	public static PlayerEffectStore load() {
		Path path = configPath();
		Map<String, String> data = new HashMap<>();

		if (Files.exists(path)) {
			try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
				Map<String, String> loaded = GSON.fromJson(reader, MAP_TYPE);
				if (loaded != null) {
					data = loaded;
				}
			} catch (IOException | RuntimeException e) {
				SikoLoginParticlesServer.LOGGER.warn(
						"[SikoLoginParticles] No se pudo leer sikologinparticles_players.json, se empieza vacio.", e);
			}
		}

		return new PlayerEffectStore(data);
	}

	public void save() {
		Path path = configPath();
		try {
			Files.createDirectories(path.getParent());
			try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
				GSON.toJson(assignments, MAP_TYPE, writer);
			}
		} catch (IOException e) {
			SikoLoginParticlesServer.LOGGER.warn("[SikoLoginParticles] No se pudo guardar sikologinparticles_players.json.", e);
		}
	}

	/** Devuelve el efecto asignado a este jugador, o vacio si no tiene ninguno (comportamiento por defecto). */
	public Optional<SikoEffectType> getEffect(String playerName) {
		String id = assignments.get(playerName.toLowerCase(Locale.ROOT));
		if (id == null) {
			return Optional.empty();
		}
		return SikoEffectType.fromId(id);
	}

	/** Asigna un efecto a un jugador, o se lo quita si type es null. */
	public void setEffect(String playerName, SikoEffectType type) {
		String key = playerName.toLowerCase(Locale.ROOT);
		if (type == null) {
			assignments.remove(key);
		} else {
			assignments.put(key, type.getId());
		}
	}

	private static Path configPath() {
		return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
	}
}
