package com.example.ccballs

import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory

object CustomCobblemonBalls : ModInitializer {

	const val MOD_ID = "ccballs"
	val LOGGER = LoggerFactory.getLogger(MOD_ID)!!

	override fun onInitialize() {
		LOGGER.info("Inicializando Custom Cobblemon Balls")

		// 1) Registra los items (por ahora como PokeBallItem "en bruto" de Cobblemon,
		//    ver ModItems.kt y las notas en README.md)
		ModItems.register()

		// 2) Registra el creative tab propio del mod
		ModItemGroups.register()

		// 3) Conecta cada item con una entrada real en el registro de Pokeball de Cobblemon
		//    (ratios de captura, throwPower, etc). Ver CustomPokeBalls.kt: esta es la parte
		//    que hay que verificar/ajustar contra la version exacta de Cobblemon instalada.
		CustomPokeBalls.register()
	}
}
