package com.siko.sikologinparticles;

import java.util.Locale;
import java.util.Optional;

/**
 * Los 3 efectos de particulas disponibles. No existe un valor "NINGUNO" aqui
 * a proposito: la ausencia de efecto se representa como "sin asignar" en
 * {@link PlayerEffectStore} (es decir, con Optional.empty()), que es el
 * comportamiento por defecto para cualquier jugador.
 */
public enum SikoEffectType {

	HOJAS("hojas"),
	SAKURA("sakura"),
	VIENTO("viento"),
	FENIX("fenix");

	/** Identificador en minusculas usado en comandos y en el archivo de datos. */
	private final String id;

	SikoEffectType(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	/** Palabra especial usada en los comandos para desactivar el efecto de un jugador. */
	public static final String NONE_KEYWORD = "ninguno";

	/** Convierte un texto de comando/almacenamiento en un efecto, si es valido. */
	public static Optional<SikoEffectType> fromId(String text) {
		String normalized = text.toLowerCase(Locale.ROOT);
		for (SikoEffectType type : values()) {
			if (type.id.equals(normalized)) {
				return Optional.of(type);
			}
		}
		return Optional.empty();
	}
}
