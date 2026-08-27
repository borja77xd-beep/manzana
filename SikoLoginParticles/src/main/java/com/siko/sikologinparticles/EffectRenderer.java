package com.siko.sikologinparticles;

import net.minecraft.block.Blocks;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.random.Random;

/**
 * Calcula y genera, tick a tick, las particulas de cada uno de los 4
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
			case FENIX -> spawnFenix(world, player, tick, duration, config);
		}
	}

	/**
	 * Remolino de hojas: cada particula nace en una posicion aleatoria
	 * alrededor del cuerpo y recibe un empuje tangencial (para que parezca
	 * que gira) mas una pequena brisa ascendente, tambien aleatoria. Al ser
	 * la particula de "bloque" (con gravedad propia), la brisa las levanta
	 * un poco y luego la gravedad las va asentando, dando una sensacion
	 * mucho mas viva y organica que un giro geometrico perfecto.
	 */
	private static void spawnHojas(ServerWorld world, ServerPlayerEntity player, int tick, int duration, ModConfig config) {
		ParticleEffect leaf = new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.OAK_LEAVES.getDefaultState());

		int perTick = Math.max(1, config.particleCount / duration);
		double baseX = player.getX();
		double baseY = player.getY();
		double baseZ = player.getZ();
		Random random = world.getRandom();

		for (int i = 0; i < perTick; i++) {
			double angle = random.nextDouble() * Math.PI * 2.0;
			double r = config.radius * (0.4 + random.nextDouble() * 0.9);
			double height = random.nextDouble() * 1.7;

			double x = baseX + Math.cos(angle) * r;
			double z = baseZ + Math.sin(angle) * r;
			double y = baseY + height;

			// Empuje tangencial (para que giren alrededor del cuerpo) mas
			// jitter aleatorio, y una brisa ascendente de intensidad variable.
			double tangential = 0.03 + random.nextDouble() * 0.03;
			double velX = -Math.sin(angle) * tangential + (random.nextDouble() - 0.5) * 0.015;
			double velZ = Math.cos(angle) * tangential + (random.nextDouble() - 0.5) * 0.015;
			double velY = 0.02 + random.nextDouble() * 0.05;

			world.spawnParticles(leaf, x, y, z, 0, velX, velY, velZ, 1.0);
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

	/**
	 * FENIX: el efecto "epico", solo con particulas de fuego y brillos (sin
	 * nada de combate). Un par de alas hechas de fuego (normal y de alma)
	 * salen de la espalda del jugador -usando su orientacion real, para que
	 * siempre aparezcan detras de el mire hacia donde mire- y se despliegan
	 * con un aleteo suave. En el pecho sube una pequena columna de fuego
	 * que hace de "cuerpo" del ave. Empieza con un estallido de chispas
	 * doradas (particula del Totem de la Inmortalidad) y termina con un haz
	 * de luz (end rod) disparandose hacia el cielo.
	 */
	private static void spawnFenix(ServerWorld world, ServerPlayerEntity player, int tick, int duration, ModConfig config) {
		double baseX = player.getX();
		double baseY = player.getY();
		double baseZ = player.getZ();
		Random random = world.getRandom();
		double progress = (double) tick / duration;

		// Ignicion inicial: solo chispas doradas, sin ningun efecto de impacto.
		if (tick == 0) {
			world.spawnParticles(ParticleTypes.TOTEM_OF_UNDYING, baseX, baseY + 1.1, baseZ,
					30, 0.6, 0.9, 0.6, 0.35);
		}

		// Direccion "hacia atras" del jugador (segun hacia donde mira), para
		// que las alas salgan siempre de la espalda.
		double yawRad = Math.toRadians(player.getYaw());
		double forwardX = -Math.sin(yawRad);
		double forwardZ = Math.cos(yawRad);
		double backX = -forwardX;
		double backZ = -forwardZ;
		double sideX = -backZ;
		double sideZ = backX;

		double scale = config.radius / 0.7; // el radio configurado escala el tamano de las alas
		double openness = Math.min(1.0, progress / 0.4) * (Math.PI * 0.55); // se despliegan en el primer 40%
		double flap = Math.sin(progress * Math.PI * 5.0) * 0.25; // aleteo suave y continuo

		int feathers = 5;
		int pointsPerFeather = 2;

		for (int side = -1; side <= 1; side += 2) {
			double rootX = baseX + sideX * side * 0.2 + backX * 0.1;
			double rootZ = baseZ + sideZ * side * 0.2 + backZ * 0.1;
			double rootY = baseY + 1.3;

			for (int k = 0; k < feathers; k++) {
				double f = (double) k / (feathers - 1);
				double theta = openness * f;

				double dirX = backX * Math.cos(theta) + sideX * side * Math.sin(theta);
				double dirZ = backZ * Math.cos(theta) + sideZ * side * Math.sin(theta);
				double dirY = flap * (0.3 + f * 0.7);

				double length = scale * (0.3 + f * 1.4);

				for (int p = 1; p <= pointsPerFeather; p++) {
					double t = (double) p / pointsPerFeather;
					double px = rootX + dirX * length * t;
					double pz = rootZ + dirZ * length * t;
					double py = rootY + dirY * length * t;

					ParticleEffect feather = (k % 2 == 0) ? ParticleTypes.FLAME : ParticleTypes.SOUL_FIRE_FLAME;
					world.spawnParticles(feather, px, py, pz, 0, 0.0, 0.01, 0.0, 1.0);
				}
			}
		}

		// Columna de fuego en el cuerpo, como si el propio jugador fuera el ave.
		if (tick % 2 == 0) {
			double bodyY = baseY + random.nextDouble() * 1.6;
			world.spawnParticles(ParticleTypes.SMALL_FLAME, baseX, bodyY, baseZ, 0, 0.0, 0.02, 0.0, 1.0);
		}

		// Remate final: haz de luz disparandose hacia el cielo desde la cabeza.
		if (progress > 0.75) {
			double beamHeight = baseY + 1.8 + (progress - 0.75) * 6.0;
			double jitterX = (random.nextDouble() - 0.5) * 0.1;
			double jitterZ = (random.nextDouble() - 0.5) * 0.1;
			world.spawnParticles(ParticleTypes.END_ROD, baseX + jitterX, beamHeight, baseZ + jitterZ,
					0, 0.0, 0.05, 0.0, 1.0);
		}
	}
}
