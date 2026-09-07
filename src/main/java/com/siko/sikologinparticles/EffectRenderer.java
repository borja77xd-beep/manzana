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
			case RAYOS -> spawnRayos(world, player, tick, duration, config);
			case PIEDRA -> spawnPiedra(world, player, tick, duration, config);
			case VACIO -> spawnVacio(world, player, tick, duration, config);
			case HIELO -> spawnHielo(world, player, tick, duration, config);
			case BUDA -> spawnBuda(world, player, tick, duration, config);
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

	/**
	 * RAYOS: chispas electricas crepitando en posiciones aleatorias
	 * alrededor del cuerpo (nada de movimiento suave, a proposito, para que
	 * parezca electricidad estatica saltando), motas de luz flotando
	 * despacio, y dos "rayos" reales durante la animacion: una linea en
	 * zigzag de chispas que cae desde el cielo hasta el jugador, rematada
	 * con un flash brillante, como si le acabara de caer un rayo encima
	 * (sin ningun dano ni sonido de trueno, solo el efecto visual).
	 */
	private static void spawnRayos(ServerWorld world, ServerPlayerEntity player, int tick, int duration, ModConfig config) {
		double baseX = player.getX();
		double baseY = player.getY();
		double baseZ = player.getZ();
		Random random = world.getRandom();

		int sparksPerTick = Math.max(1, config.particleCount / duration);
		for (int i = 0; i < sparksPerTick; i++) {
			double angle = random.nextDouble() * Math.PI * 2.0;
			double r = config.radius * (0.5 + random.nextDouble() * 0.9);
			double height = random.nextDouble() * 1.9;

			double x = baseX + Math.cos(angle) * r;
			double z = baseZ + Math.sin(angle) * r;
			double y = baseY + height;

			double vx = (random.nextDouble() - 0.5) * 0.05;
			double vy = (random.nextDouble() - 0.5) * 0.05;
			double vz = (random.nextDouble() - 0.5) * 0.05;

			world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 0, vx, vy, vz, 1.0);
		}

		// Motas de luz ambiental, mas lentas y esparcidas, para dar sensacion de "luces".
		if (tick % 3 == 0) {
			double angle = random.nextDouble() * Math.PI * 2.0;
			double r = config.radius * 1.3;
			double x = baseX + Math.cos(angle) * r;
			double z = baseZ + Math.sin(angle) * r;
			double y = baseY + 0.3 + random.nextDouble() * 1.6;
			world.spawnParticles(ParticleTypes.GLOW, x, y, z, 0, 0.0, 0.01, 0.0, 1.0);
		}

		// Dos rayos reales durante la animacion (al 30% y al 65% del tiempo).
		if (tick == (int) (duration * 0.3) || tick == (int) (duration * 0.65)) {
			spawnBoltStrike(world, baseX, baseY, baseZ, random);
		}
	}

	/** Dibuja una linea en zigzag de chispas cayendo del cielo, rematada con un flash. */
	private static void spawnBoltStrike(ServerWorld world, double baseX, double baseY, double baseZ, Random random) {
		double startHeight = 6.0;
		int segments = 10;
		double x = baseX;
		double z = baseZ;

		for (int i = 0; i < segments; i++) {
			double t = (double) i / segments;
			double y = baseY + startHeight * (1.0 - t);
			x += (random.nextDouble() - 0.5) * 0.4;
			z += (random.nextDouble() - 0.5) * 0.4;
			world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 0, 0.0, 0.0, 0.0, 0.0);
		}

		world.spawnParticles(ParticleTypes.FLASH, baseX, baseY + 0.2, baseZ, 1, 0.1, 0.1, 0.1, 0.0);
	}

	/**
	 * PIEDRA: la animacion "al reves" de las demas. En vez de crecer, el
	 * jugador empieza envuelto en una cascara de piedra pegada al cuerpo
	 * (particula de "bloque" con la textura vanilla de la piedra) que se va
	 * desprendiendo a trozos con el tiempo: cuanta mas cascara queda, mas
	 * frecuentes son los trozos que salen despedidos; segun avanza la
	 * animacion, hay cada vez menos densidad y menos desprendimientos,
	 * hasta acabar en una nube de polvo (particula "poof") que se disipa,
	 * dejando al jugador completamente libre.
	 */
	private static void spawnPiedra(ServerWorld world, ServerPlayerEntity player, int tick, int duration, ModConfig config) {
		ParticleEffect stone = new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.STONE.getDefaultState());

		double baseX = player.getX();
		double baseY = player.getY();
		double baseZ = player.getZ();
		Random random = world.getRandom();

		double progress = (double) tick / duration;
		double remaining = 1.0 - progress; // cascara restante: 1.0 (completa) -> 0.0 (nada)

		int basePerTick = Math.max(1, config.particleCount / duration);

		// Cascara todavia pegada al cuerpo: cuanta menos "remaining" quede,
		// menos densa se dibuja (casi estatica, solo un leve temblor).
		int shellCount = (int) Math.round(basePerTick * 2.2 * remaining);
		for (int i = 0; i < shellCount; i++) {
			double angle = random.nextDouble() * Math.PI * 2.0;
			double height = random.nextDouble() * 1.9;
			double r = config.radius * 0.65;

			double x = baseX + Math.cos(angle) * r;
			double z = baseZ + Math.sin(angle) * r;
			double y = baseY + height;

			double jitter = 0.008;
			world.spawnParticles(stone, x, y, z, 0,
					(random.nextDouble() - 0.5) * jitter,
					(random.nextDouble() - 0.5) * jitter,
					(random.nextDouble() - 0.5) * jitter, 1.0);
		}

		// Trozos que se desprenden y salen despedidos hacia fuera: cuanto
		// mas cascara quede por romper, mas probable que salte un trozo.
		if (random.nextDouble() < remaining * 0.7) {
			double angle = random.nextDouble() * Math.PI * 2.0;
			double height = random.nextDouble() * 1.9;
			double r = config.radius * 0.65;

			double x = baseX + Math.cos(angle) * r;
			double z = baseZ + Math.sin(angle) * r;
			double y = baseY + height;

			double outSpeed = 0.05 + random.nextDouble() * 0.08;
			double outX = Math.cos(angle) * outSpeed;
			double outZ = Math.sin(angle) * outSpeed;
			double outY = 0.02 + random.nextDouble() * 0.05;

			world.spawnParticles(stone, x, y, z, 0, outX, outY, outZ, 1.0);
		}

		// Ultimo tramo: una nube de polvo tenue, como si se disiparan los
		// ultimos restos de la cascara.
		if (progress > 0.75 && tick % 3 == 0) {
			double angle = random.nextDouble() * Math.PI * 2.0;
			double r = config.radius * 0.65;
			double x = baseX + Math.cos(angle) * r;
			double z = baseZ + Math.sin(angle) * r;
			double y = baseY + random.nextDouble() * 1.9;
			world.spawnParticles(ParticleTypes.POOF, x, y, z, 0, 0.0, 0.02, 0.0, 1.0);
		}
	}

	/**
	 * VACIO: un remolino de particulas de portal (la misma familia que usa
	 * el propio juego para los portales del End y el teletransporte de los
	 * enderman) que converge desde fuera hacia el jugador, como si lo
	 * estuvieran materializando desde el vacio. A proposito usa una unica
	 * particula vanilla de principio a fin -sin mezclar ninguna otra
	 * tematica- para que todo el efecto se sienta como una sola cosa
	 * coherente. Cerca del final, el remolino se cierra en un anillo
	 * ajustado justo alrededor del cuerpo, como el instante final de la
	 * materializacion.
	 */
	private static void spawnVacio(ServerWorld world, ServerPlayerEntity player, int tick, int duration, ModConfig config) {
		double baseX = player.getX();
		double baseY = player.getY();
		double baseZ = player.getZ();
		Random random = world.getRandom();

		int perTick = Math.max(1, config.particleCount / duration);
		double startRadius = config.radius * 3.0;

		for (int i = 0; i < perTick; i++) {
			double subStep = (double) i / perTick;
			double progress = (tick + subStep) / duration;

			double angle = progress * 5.0 * Math.PI * 2.0;
			// El radio arranca lejos y converge hacia un anillo pegado al cuerpo.
			double r = startRadius * (1.0 - progress) + config.radius * 0.2 * progress;
			double height = random.nextDouble() * 1.9;

			double x = baseX + Math.cos(angle) * r;
			double z = baseZ + Math.sin(angle) * r;
			double y = baseY + height;

			// Sin velocidad propia: la posicion ya la controla la espiral.
			world.spawnParticles(ParticleTypes.PORTAL, x, y, z, 0, 0.0, 0.0, 0.0, 0.0);
		}
	}

	/**
	 * HIELO: copos de nieve (particula vanilla de la nieve en polvo)
	 * cayendo despacio y esparcidos alrededor del cuerpo, con destellos de
	 * escarcha ocasionales (particula de "bloque" con la textura vanilla
	 * real del hielo) formandose junto al jugador. Solo estas dos
	 * particulas, ambas de la misma familia fria, sin mezclar nada mas.
	 */
	private static void spawnHielo(ServerWorld world, ServerPlayerEntity player, int tick, int duration, ModConfig config) {
		ParticleEffect iceShard = new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.ICE.getDefaultState());

		double baseX = player.getX();
		double baseY = player.getY();
		double baseZ = player.getZ();
		Random random = world.getRandom();

		int perTick = Math.max(1, config.particleCount / duration);

		// Copos de nieve naciendo por encima del jugador y cayendo despacio,
		// con una leve deriva lateral, esparcidos alrededor del cuerpo.
		for (int i = 0; i < perTick; i++) {
			double angle = random.nextDouble() * Math.PI * 2.0;
			double r = random.nextDouble() * config.radius * 1.6;

			double x = baseX + Math.cos(angle) * r;
			double z = baseZ + Math.sin(angle) * r;
			double y = baseY + 1.4 + random.nextDouble() * 0.8;

			double driftX = (random.nextDouble() - 0.5) * 0.02;
			double driftZ = (random.nextDouble() - 0.5) * 0.02;
			double fallY = -0.03 - random.nextDouble() * 0.02;

			world.spawnParticles(ParticleTypes.SNOWFLAKE, x, y, z, 0, driftX, fallY, driftZ, 1.0);
		}

		// Un destello de escarcha ocasional, como si se formara en el momento.
		if (tick % 6 == 0) {
			double angle = random.nextDouble() * Math.PI * 2.0;
			double r = config.radius * 0.5;
			double x = baseX + Math.cos(angle) * r;
			double z = baseZ + Math.sin(angle) * r;
			double y = baseY + random.nextDouble() * 1.7;

			world.spawnParticles(iceShard, x, y, z, 0, 0.0, 0.0, 0.0, 1.0);
		}
	}

	/**
	 * BUDA: dibuja el manji budista (el simbolo tradicional del budismo,
	 * hinduismo y jainismo -usado incluso hoy en los mapas de Japon para
	 * senalar templos-, en su orientacion vertical tradicional, sin ningun
	 * giro de 45 grados) flotando en horizontal sobre la cabeza del
	 * jugador. Se traza poco a poco con particulas de end_rod (una unica
	 * particula, luz calida), se mantiene formado unos instantes y se
	 * disuelve en el ultimo tramo, como si el propio simbolo se deshiciera
	 * en luz.
	 */
	private static void spawnBuda(ServerWorld world, ServerPlayerEntity player, int tick, int duration, ModConfig config) {
		double baseX = player.getX();
		double baseY = player.getY() + 2.3;
		double baseZ = player.getZ();
		Random random = world.getRandom();

		double armLength = config.radius * 1.6;
		double footLength = armLength * 0.55;

		// Los 4 brazos (centro -> punta) mas los 4 "pies" (la parte doblada
		// de cada brazo), en el sentido tradicional del manji budista.
		double[][][] segments = {
				{{0, 0}, {0, -armLength}}, // brazo superior
				{{0, -armLength}, {-footLength, -armLength}}, // pie: dobla a la izquierda
				{{0, 0}, {armLength, 0}}, // brazo derecho
				{{armLength, 0}, {armLength, -footLength}}, // pie: dobla hacia arriba
				{{0, 0}, {0, armLength}}, // brazo inferior
				{{0, armLength}, {footLength, armLength}}, // pie: dobla a la derecha
				{{0, 0}, {-armLength, 0}}, // brazo izquierdo
				{{-armLength, 0}, {-armLength, footLength}}, // pie: dobla hacia abajo
		};

		double progress = (double) tick / duration;
		int perTick = Math.max(2, config.particleCount / duration);

		if (progress <= 0.75) {
			// Fase de trazado + mantenimiento: se dibuja del todo en el
			// primer 45% del tiempo, y se queda quieto (con leve brillo)
			// hasta el 75%.
			double drawProgress = Math.min(1.0, progress / 0.45);
			double drawnLength = drawProgress * segments.length;

			for (int i = 0; i < perTick; i++) {
				double u = random.nextDouble() * drawnLength;
				int segIndex = Math.min(segments.length - 1, (int) u);
				double t = u - segIndex;

				double[] start = segments[segIndex][0];
				double[] end = segments[segIndex][1];
				double px = start[0] + (end[0] - start[0]) * t;
				double pz = start[1] + (end[1] - start[1]) * t;

				double x = baseX + px;
				double z = baseZ + pz;
				double y = baseY + (random.nextDouble() - 0.5) * 0.05;

				world.spawnParticles(ParticleTypes.END_ROD, x, y, z, 0, 0.0, 0.005, 0.0, 1.0);
			}
		} else {
			// Fase de disolucion: los puntos del simbolo se sueltan hacia
			// fuera y hacia arriba, como si se deshiciera en luz.
			for (int i = 0; i < perTick; i++) {
				double u = random.nextDouble() * segments.length;
				int segIndex = Math.min(segments.length - 1, (int) u);
				double t = u - segIndex;

				double[] start = segments[segIndex][0];
				double[] end = segments[segIndex][1];
				double px = start[0] + (end[0] - start[0]) * t;
				double pz = start[1] + (end[1] - start[1]) * t;

				double distance = Math.max(0.01, Math.sqrt(px * px + pz * pz));
				double outSpeed = 0.02 + random.nextDouble() * 0.03;
				double vx = (px / distance) * outSpeed;
				double vz = (pz / distance) * outSpeed;
				double vy = 0.02 + random.nextDouble() * 0.03;

				double x = baseX + px;
				double z = baseZ + pz;

				world.spawnParticles(ParticleTypes.END_ROD, x, baseY, z, 0, vx, vy, vz, 1.0);
			}
		}
	}
}
