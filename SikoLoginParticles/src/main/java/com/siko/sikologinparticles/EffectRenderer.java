package com.siko.sikologinparticles;

import net.minecraft.block.Blocks;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.random.Random;

/**
 * Calcula y genera, tick a tick, las particulas de cada uno de los 3
 * efectos. Todas las particulas se generan con
 * {@link ServerWorld#spawnParticles} usando count=0 (una sola particula por
 * llamada, en la posicion exacta calculada), lo que hace que el servidor las
 * retransmita automaticamente a todos los jugadores cercanos que las puedan
 * ver, sin que ellos necesiten tener el mod instalado.
 */
public final class EffectRenderer {

	private EffectRenderer() {
	}

	public static void spawnTick(ServerWorld world, ServerPlayerEntity player, SikoEffectType type,
			int tick, int duration, ModConfig config) {
		switch (type) {
			case HOJAS -> spawnHojas(world, player, tick, duration, config);
			case SAKURA -> spawnSakura(world, player, tick, duration, config);
			case VIENTO -> spawnViento(world, player, tick, duration, config);
		}
	}

	/**
	 * Remolino de hojas: un anillo de particulas de "bloque" usando la
	 * textura vanilla de hojas de roble, girando suavemente alrededor del
	 * cuerpo del jugador con un ligero balanceo, como si el viento las
	 * arrastrara. No es una espiral perfecta a proposito: un giro con
	 * pequenas variaciones de radio y altura resulta mucho mas "natural"
	 * que un movimiento geometrico perfecto.
	 */
	private static void spawnHojas(ServerWorld world, ServerPlayerEntity player, int tick, int duration, ModConfig config) {
		ParticleEffect leaf = new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.OAK_LEAVES.getDefaultState());

		int perTick = Math.max(1, config.particleCount / duration);
		double baseX = player.getX();
		double baseY = player.getY();
		double baseZ = player.getZ();

		for (int i = 0; i < perTick; i++) {
			double subStep = (double) i / perTick;
			double progress = (tick + subStep) / duration;

			double angle = progress * 3.0 * Math.PI * 2.0 + i * 0.7;
			double wobble = Math.sin(progress * Math.PI * 6.0) * 0.15;
			double r = config.radius + wobble;
			double height = 0.2 + progress * 1.6;

			double x = baseX + Math.cos(angle) * r;
			double z = baseZ + Math.sin(angle) * r;
			double y = baseY + height;

			// Ligera deriva hacia abajo y hacia los lados, como una hoja real cayendo.
			world.spawnParticles(leaf, x, y, z, 0, 0.01, -0.02, 0.01, 1.0);
		}
	}

	/**
	 * Petalos de cerezo cayendo desde arriba, con una deriva lateral suave,
	 * usando la particula vanilla dedicada a las hojas de cerezo.
	 */
	private static void spawnSakura(ServerWorld world, ServerPlayerEntity player, int tick, int duration, ModConfig config) {
		int perTick = Math.max(1, config.particleCount / duration);
		double baseX = player.getX();
		double baseY = player.getY();
		double baseZ = player.getZ();
		Random random = world.getRandom();

		for (int i = 0; i < perTick; i++) {
			double angle = random.nextDouble() * Math.PI * 2.0;
			double r = random.nextDouble() * config.radius * 1.8;

			double x = baseX + Math.cos(angle) * r;
			double z = baseZ + Math.sin(angle) * r;
			double y = baseY + 2.0 + random.nextDouble() * 0.6;

			double driftX = (random.nextDouble() - 0.5) * 0.02;
			double driftZ = (random.nextDouble() - 0.5) * 0.02;

			world.spawnParticles(ParticleTypes.CHERRY_LEAVES, x, y, z, 0, driftX, -0.03, driftZ, 1.0);
		}
	}

	/**
	 * Torbellino de viento: espiral ascendente que nace en los pies del
	 * jugador y sube girando, usando la particula vanilla del Breeze /
	 * carga de viento.
	 */
	private static void spawnViento(ServerWorld world, ServerPlayerEntity player, int tick, int duration, ModConfig config) {
		int perTick = Math.max(1, config.particleCount / duration);
		double baseX = player.getX();
		double baseY = player.getY();
		double baseZ = player.getZ();

		for (int i = 0; i < perTick; i++) {
			double subStep = (double) i / perTick;
			double progress = (tick + subStep) / duration;

			double angle = progress * 3.0 * Math.PI * 2.0;
			double r = config.radius * (1.0 - 0.3 * progress);
			double height = progress * 1.9;

			double x = baseX + Math.cos(angle) * r;
			double z = baseZ + Math.sin(angle) * r;
			double y = baseY + height;

			world.spawnParticles(ParticleTypes.GUST, x, y, z, 0, 0.0, 0.02, 0.0, 1.0);
		}
	}
}
