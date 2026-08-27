package com.example.ccballs

// ======================================================================================
// AVISO IMPORTANTE (leer antes de compilar)
// ======================================================================================
// Los imports y llamadas de abajo (PokeBalls, PokeBall, MultiplierModifier, etc.) son
// clases INTERNAS de Cobblemon: no forman parte de una API publica/estable documentada,
// y sus nombres exactos de metodo pueden variar entre versiones (1.5.x, 1.6.x, 1.7.x...).
//
// Lo que SI esta confirmado (verificado contra bytecode real de Cobblemon por el mod
// "Catch Rate Display"):
//   - Cada PokeBall tiene una propiedad "catchRateModifier" que puede ser null (=1x)
//   - La clase "MultiplierModifier(value: Float)" existe y aplica un multiplicador fijo
//
// Lo que TENES que verificar vos mismo contra el jar de Cobblemon que estas usando
// (ver README.md, seccion "Como verificar la API real de tu version"):
//   - El paquete exacto de PokeBall / PokeBalls / MultiplierModifier
//   - El nombre exacto de la funcion de registro (puede ser PokeBalls.register(...),
//     PokeBalls.registerPokeball(...), o requerir pasar tambien un "builder")
//   - Si el constructor de PokeBall pide mas parametros obligatorios en tu version
//
// Import de ejemplo (AJUSTAR segun lo que encuentres al decompilar):
// import com.cobblemon.mod.common.api.pokeball.PokeBalls
// import com.cobblemon.mod.common.api.pokeball.catching.MultiplierModifier
// import com.cobblemon.mod.common.pokeball.PokeBall
// import net.minecraft.resources.ResourceLocation
// ======================================================================================

import com.cobblemon.mod.common.api.pokeball.PokeBalls
import com.cobblemon.mod.common.api.pokeball.catching.MultiplierModifier
import com.cobblemon.mod.common.pokeball.PokeBall
import net.minecraft.resources.ResourceLocation

object CustomPokeBalls {

	private fun id(path: String): ResourceLocation =
		ResourceLocation.fromNamespaceAndPath(CustomCobblemonBalls.MOD_ID, path)

	// --------------------------------------------------------------------------------
	// SikkoBall: nivel Ultra Ball o mejor. La Ultra Ball vanilla de Cobblemon es 2.0x,
	// asi que dejamos esta claramente por encima.
	// --------------------------------------------------------------------------------
	val SIKKO_BALL: PokeBall = PokeBall(
		name = id("sikko_ball"),
		catchRateModifier = MultiplierModifier(2.5F)
	)

	// --------------------------------------------------------------------------------
	// Tournament SikkoBall: nivel Master Ball (captura prácticamente garantizada).
	//
	// OJO: en Cobblemon, la Master Ball NO funciona como un multiplicador mas alto
	// nomas -- su 100% de captura es un caso especial que salta el calculo normal
	// (multiplicar por mas y mas no te da un 100% matematico, solo se le acerca).
	// Como placeholder pongo un multiplicador enorme (100x), que en la practica va a
	// capturar casi siempre. Para el 100% real como la Master Ball, hay que encontrar
	// en el PokeBalls.kt decompilado como esta definida MASTER_BALL y copiar exactamente
	// ese mismo catchRateModifier/mecanismo aca. Ver README para el paso a paso.
	// --------------------------------------------------------------------------------
	val TOURNAMENT_SIKKO_BALL: PokeBall = PokeBall(
		name = id("tournament_sikko_ball"),
		catchRateModifier = MultiplierModifier(100.0F) // TODO: reemplazar por el mecanismo real de la Master Ball
	)

	// --------------------------------------------------------------------------------
	// GS Ball: tambien nivel Master Ball (captura prácticamente garantizada), con la
	// misma salvedad que la Tournament SikkoBall de arriba respecto al 100% real.
	// Ademas pediste logica condicional para esta ball (ej. comportamiento especial
	// legendario/mitico) -- eso se implementa en CustomCatchLogic.kt via eventos de
	// Cobblemon, independientemente del catchRateModifier base de aca.
	// --------------------------------------------------------------------------------
	val GS_BALL: PokeBall = PokeBall(
		name = id("gs_ball"),
		catchRateModifier = MultiplierModifier(100.0F) // TODO: reemplazar por el mecanismo real de la Master Ball
	)

	// --------------------------------------------------------------------------------
	// Escoria Ball: deliberadamente floja, la "peor" ball del set (multiplicador < 1x).
	// Si en vez de "mas debil" queres que directamente NUNCA capture salvo excepciones,
	// eso tambien se resuelve en CustomCatchLogic.kt cancelando la captura.
	// --------------------------------------------------------------------------------
	val ESCORIA_BALL: PokeBall = PokeBall(
		name = id("escoria_ball"),
		catchRateModifier = MultiplierModifier(0.5F)
	)

	fun register() {
		// AJUSTAR el nombre de esta funcion si tu version de Cobblemon usa otro (ver aviso arriba)
		PokeBalls.register(SIKKO_BALL)
		PokeBalls.register(TOURNAMENT_SIKKO_BALL)
		PokeBalls.register(GS_BALL)
		PokeBalls.register(ESCORIA_BALL)

		CustomCobblemonBalls.LOGGER.info("Registradas 4 pokeballs personalizadas")
	}
}
