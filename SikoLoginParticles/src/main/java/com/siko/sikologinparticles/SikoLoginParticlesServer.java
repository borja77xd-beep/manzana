package com.siko.sikologinparticles;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Punto de entrada del mod. Es intencionadamente server-side: el efecto debe
 * verse por todos los jugadores cercanos y solo los operadores pueden
 * asignarlo, lo que requiere autoridad de servidor (no se puede hacer de
 * forma fiable solo desde el cliente). Al generar las particulas con
 * ServerWorld#spawnParticles, estas se retransmiten automaticamente a todos
 * los clientes cercanos sin que ellos necesiten instalar nada.
 * <p>
 * Deteccion de "entrada al servidor": se usa
 * {@link ServerPlayConnectionEvents#JOIN}, el equivalente exacto en el
 * servidor del evento client-side JOIN: se dispara una unica vez por
 * conexion, y no en cada cambio de chunk, respawn o cambio de dimension
 * dentro de la misma conexion.
 */
public class SikoLoginParticlesServer implements ModInitializer {

	public static final String MOD_ID = "sikologinparticles";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModConfig config = ModConfig.load();
		PlayerEffectStore store = PlayerEffectStore.load();
		AnimationManager animationManager = new AnimationManager(config);

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayerEntity player = handler.getPlayer();
			store.getEffect(player.getGameProfile().getName())
					.ifPresent(type -> animationManager.scheduleOnJoin(player, type));
		});

		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
				animationManager.cancel(handler.getPlayer().getUuid()));

		ServerTickEvents.END_SERVER_TICK.register(animationManager::tick);

		SikoEffectsCommand.register(store, animationManager);

		LOGGER.info("[SikoLoginParticles] Mod inicializado (enabled={}).", config.enabled);
	}
}
