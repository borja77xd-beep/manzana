package com.siko.sikologinparticles;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.sound.SoundEvent;

/**
 * Controla la animacion de particulas en espiral.
 * <p>
 * Toda la logica vive en un unico metodo ({@link #tick(MinecraftClient)}) que
 * se llama una vez por tick de cliente. Cuando no hay ninguna animacion en
 * curso, el metodo no hace practicamente nada (dos comparaciones enteras), asi
 * que el coste en reposo es insignificante.
 */
public class LoginParticleAnimator {

	/** Numero aproximado de vueltas que da la espiral durante toda la animacion. */
	private static final double SPIRAL_TURNS = 2.25;

	/** Altura aproximada hasta la que sube la espiral (cabeza de un jugador de pie). */
	private static final double TOP_HEIGHT = 1.9;

	private final ModConfig config;

	// -1 significa "inactivo" en ambos contadores.
	private int delayCounter = -1;
	private int animationTick = -1;

	public LoginParticleAnimator(ModConfig config) {
		this.config = config;
	}

	/** Programa el inicio de la animacion tras el retardo configurado. */
	public void scheduleStart() {
		if (!config.enabled) {
			return;
		}
		this.delayCounter = Math.max(0, config.delayTicks);
		this.animationTick = -1;
	}

	/** Cancela cualquier retardo o animacion en curso (por ejemplo, al desconectar). */
	public void reset() {
		this.delayCounter = -1;
		this.animationTick = -1;
	}

	/**
	 * Debe llamarse una vez por tick de cliente (ClientTickEvents.END_CLIENT_TICK).
	 */
	public void tick(MinecraftClient client) {
		ClientPlayerEntity player = client.player;
		ClientWorld world = client.world;

		if (player == null || world == null) {
			// Sin jugador/mundo no hay donde dibujar nada; se aborta con seguridad.
			reset();
			return;
		}

		// Fase de espera (delay_ticks).
		if (delayCounter > 0) {
			delayCounter--;
			return;
		}
		if (delayCounter == 0) {
			delayCounter = -1;
			animationTick = 0;
			if (config.playSound) {
				playStartSound(player);
			}
		}

		// Fase de animacion activa.
		if (animationTick < 0) {
			return;
		}

		int duration = Math.max(1, config.durationTicks);
		if (animationTick >= duration) {
			animationTick = -1; // Termina limpiamente, sin dejar nada activo.
			return;
		}

		spawnTickParticles(world, player, animationTick, duration);
		animationTick++;
	}

	private void playStartSound(ClientPlayerEntity player) {
		SoundEvent sound = config.resolveSound();
		player.playSound(sound, config.soundVolume, 1.0f);
	}

	/**
	 * Calcula y genera las particulas correspondientes a este tick de la
	 * animacion. La posicion se calcula matematicamente en funcion del
	 * progreso (tick actual / duracion total) para formar una espiral
	 * ascendente alrededor del jugador.
	 */
	private void spawnTickParticles(ClientWorld world, ClientPlayerEntity player, int tick, int duration) {
		ParticleEffect particle = config.resolveParticle();

		// Reparte las particulas configuradas a lo largo de toda la duracion.
		int particlesPerTick = Math.max(1, config.particleCount / duration);

		double baseX = player.getX();
		double baseY = player.getY();
		double baseZ = player.getZ();

		for (int i = 0; i < particlesPerTick; i++) {
			// Progreso fraccionario suave: combina el tick actual con la
			// sub-particula "i" para que, si hay varias por tick, no queden
			// todas apiladas en el mismo punto del anillo.
			double subStep = (double) i / particlesPerTick;
			double progress = (tick + subStep) / duration; // 0.0 -> 1.0

			double angle = progress * SPIRAL_TURNS * (Math.PI * 2.0);
			double height = progress * TOP_HEIGHT;

			// El radio se reduce ligeramente segun sube, dando una sensacion
			// de que la espiral "converge" suavemente en vez de ser un cilindro.
			double radius = config.radius * (1.0 - 0.35 * progress);

			double x = baseX + Math.cos(angle) * radius;
			double z = baseZ + Math.sin(angle) * radius;
			double y = baseY + height;

			// Velocidad minima hacia arriba para dar sensacion de ligereza,
			// sin que la particula se aleje demasiado del trazado calculado.
			double velocityY = 0.01;

			world.addParticle(particle, x, y, z, 0.0, velocityY, 0.0);
		}
	}
}
