package com.siko.sikologinparticles;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Controla, para cada jugador, la espera (delay_ticks) y la animacion activa
 * (duration_ticks). Todo el trabajo cuelga de un unico metodo llamado una vez
 * por tick de servidor; si no hay nada pendiente ni activo, no hace nada.
 */
public class AnimationManager {

	private static class Animation {
		final SikoEffectType type;
		int tick;
		final int duration;

		Animation(SikoEffectType type, int duration) {
			this.type = type;
			this.tick = 0;
			this.duration = duration;
		}
	}

	private final ModConfig config;

	private final Map<UUID, Integer> pendingDelay = new HashMap<>();
	private final Map<UUID, SikoEffectType> pendingType = new HashMap<>();
	private final Map<UUID, Animation> active = new HashMap<>();

	public AnimationManager(ModConfig config) {
		this.config = config;
	}

	/** Programa el efecto tras el retardo configurado (usado al entrar al mundo). */
	public void scheduleOnJoin(ServerPlayerEntity player, SikoEffectType type) {
		if (!config.enabled) {
			return;
		}
		UUID uuid = player.getUuid();
		pendingDelay.put(uuid, Math.max(0, config.delayTicks));
		pendingType.put(uuid, type);
	}

	/** Reproduce el efecto de inmediato, sin esperar el retardo (usado por el comando debug). */
	public void startImmediate(ServerPlayerEntity player, SikoEffectType type) {
		UUID uuid = player.getUuid();
		active.put(uuid, new Animation(type, Math.max(1, config.durationTicks)));
		if (config.playSound) {
			playSound(player);
		}
	}

	/** Cancela cualquier retardo o animacion en curso para este jugador (por ejemplo, al desconectar). */
	public void cancel(UUID uuid) {
		pendingDelay.remove(uuid);
		pendingType.remove(uuid);
		active.remove(uuid);
	}

	/** Debe llamarse una vez por tick de servidor (ServerTickEvents.END_SERVER_TICK). */
	public void tick(MinecraftServer server) {
		if (!pendingDelay.isEmpty()) {
			List<UUID> readyToStart = new ArrayList<>();
			Iterator<Map.Entry<UUID, Integer>> it = pendingDelay.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<UUID, Integer> entry = it.next();
				int remaining = entry.getValue() - 1;
				if (remaining <= 0) {
					readyToStart.add(entry.getKey());
					it.remove();
				} else {
					entry.setValue(remaining);
				}
			}

			for (UUID uuid : readyToStart) {
				SikoEffectType type = pendingType.remove(uuid);
				ServerPlayerEntity player = server.getPlayerManager().getPlayer(uuid);
				if (player != null && type != null) {
					active.put(uuid, new Animation(type, Math.max(1, config.durationTicks)));
					if (config.playSound) {
						playSound(player);
					}
				}
			}
		}

		if (!active.isEmpty()) {
			Iterator<Map.Entry<UUID, Animation>> it = active.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<UUID, Animation> entry = it.next();
				ServerPlayerEntity player = server.getPlayerManager().getPlayer(entry.getKey());
				if (player == null) {
					it.remove();
					continue;
				}

				Animation animation = entry.getValue();
				ServerWorld world = (ServerWorld) player.getWorld();
				EffectRenderer.spawnTick(world, player, animation.type, animation.tick, animation.duration, config);
				animation.tick++;

				if (animation.tick >= animation.duration) {
					it.remove();
				}
			}
		}
	}

	private void playSound(ServerPlayerEntity player) {
		SoundEvent sound = config.resolveSound();
		player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
				sound, SoundCategory.PLAYERS, config.soundVolume, 1.0f);
	}
}
