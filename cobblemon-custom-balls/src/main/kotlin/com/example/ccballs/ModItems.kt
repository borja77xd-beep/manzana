package com.example.ccballs

// AVISO: PokeBallItem es la clase de item que usa Cobblemon para sus propias balls.
// Igual que en CustomPokeBalls.kt, el paquete/constructor exacto puede variar segun version;
// ajusta el import y la forma de instanciar si tu decompilado difiere.
import com.cobblemon.mod.common.item.PokeBallItem
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item

object ModItems {

	private fun id(path: String): ResourceLocation =
		ResourceLocation.fromNamespaceAndPath(CustomCobblemonBalls.MOD_ID, path)

	// Notar: NO se define ningun "Item.Properties().food(...)" ni receta -> sin crafting.
	// La obtencion es solo via creative tab (ModItemGroups) o comandos/loot tables que agregues vos.

	val SIKKO_BALL: Item = PokeBallItem(CustomPokeBalls.SIKKO_BALL)
	val TOURNAMENT_SIKKO_BALL: Item = PokeBallItem(CustomPokeBalls.TOURNAMENT_SIKKO_BALL)
	val GS_BALL: Item = PokeBallItem(CustomPokeBalls.GS_BALL)
	val ESCORIA_BALL: Item = PokeBallItem(CustomPokeBalls.ESCORIA_BALL)

	fun register() {
		registerItem("sikko_ball", SIKKO_BALL)
		registerItem("tournament_sikko_ball", TOURNAMENT_SIKKO_BALL)
		registerItem("gs_ball", GS_BALL)
		registerItem("escoria_ball", ESCORIA_BALL)

		CustomCobblemonBalls.LOGGER.info("Registrados 4 items de pokeball")
	}

	private fun registerItem(path: String, item: Item) {
		net.minecraft.core.Registry.register(BuiltInRegistries.ITEM, id(path), item)
	}
}
