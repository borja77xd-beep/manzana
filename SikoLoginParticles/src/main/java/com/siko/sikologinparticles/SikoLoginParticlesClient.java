package com.siko.sikologinparticles;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Punto de entrada del mod (100% client-side).
 * <p>
 * Deteccion de "entrada a un mundo/servidor":
 * Se usa {@link ClientPlayConnectionEvents#JOIN}, el evento oficial de
 * Fabric API que se dispara cuando el ClientPlayNetworkHandler del cliente
 * queda listo para enviar/recibir paquetes en la fase de juego. Este evento
 * se dispara exactamente una vez por conexion (mundo singleplayer, servidor
 * dedicado, o reconexion a traves de un proxy), y NO se vuelve a disparar por
 * cambios de chunk, respawns tras la muerte o cambios de dimension dentro de
 * la MISMA conexion. Esto encaja de forma natural con los requisitos:
 * - Se ejecuta una sola vez por conexion/entrada al mundo.
 * - No se ejecuta en cada cambio de chunk ni al reaparecer tras morir.
 * <p>
 * NOTA SOBRE PROXIES (Velocity y similares):
 * Si el proxy usa el paquete vanilla de "transfer" (disponible desde la
 * 1.20.5) para mover al jugador de un backend a otro, el cliente realiza una
 * reconexion real y este evento se vuelve a disparar automaticamente, tal
 * como pide la especificacion.
 * Sin embargo, muchas redes basadas en Velocity cambian de servidor backend
 * SIN abrir una nueva conexion TCP: desde el punto de vista del cliente,
 * este tipo de cambio de servidor llega como un paquete de "respawn" /
 * cambio de dimension, exactamente igual que un respawn normal tras morir o
 * que viajar a travesw de un portal del Nether. No existe ninguna forma
 * 100% fiable de distinguir, solo desde el cliente y sin logica de red
 * propia, un "cambio de servidor por el proxy" de un "respawn/cambio de
 * dimension normal" -- ambos generan la misma secuencia de paquetes. Intentar
 * adivinarlo (por ejemplo, disparando la animacion en cada cambio de
 * dimension) violaria el requisito de no repetir la animacion en cada
 * respawn. Por eso este mod se limita a JOIN, que es la unica senal
 * verificable y fiable de forma client-side.
 */
public class SikoLoginParticlesClient implements ClientModInitializer {

	public static final String MOD_ID = "sikologinparticles";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private ModConfig config;
	private LoginParticleAnimator animator;

	@Override
	public void onInitializeClient() {
		this.config = ModConfig.load();
		this.animator = new LoginParticleAnimator(config);

		// Se dispara una vez por conexion (ver documentacion de la clase).
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> animator.scheduleStart());

		// Evita que quede una animacion "colgada" si el jugador se
		// desconecta mientras esta en curso.
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> animator.reset());

		// Unico lugar donde se hace trabajo por tick, y solo hace algo
		// mientras hay un retardo o una animacion activos.
		ClientTickEvents.END_CLIENT_TICK.register(animator::tick);

		LOGGER.info("[SikoLoginParticles] Mod inicializado (enabled={}).", config.enabled);
	}
}
