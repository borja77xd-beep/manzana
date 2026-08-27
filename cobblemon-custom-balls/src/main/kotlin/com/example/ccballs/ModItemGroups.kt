package com.example.ccballs

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.CreativeModeTab

object ModItemGroups {

	private val CCBALLS_TAB_ID: ResourceLocation =
		ResourceLocation.fromNamespaceAndPath(CustomCobblemonBalls.MOD_ID, "ccballs_tab")

	val CCBALLS_TAB: CreativeModeTab = FabricItemGroup.builder()
		.title(Component.translatable("itemGroup.ccballs.ccballs_tab"))
		.icon { ItemStack(ModItems.GS_BALL) }
		.displayItems { _, entries ->
			entries.accept(ModItems.SIKKO_BALL)
			entries.accept(ModItems.TOURNAMENT_SIKKO_BALL)
			entries.accept(ModItems.GS_BALL)
			entries.accept(ModItems.ESCORIA_BALL)
		}
		.build()

	fun register() {
		net.minecraft.core.Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, CCBALLS_TAB_ID, CCBALLS_TAB)
		CustomCobblemonBalls.LOGGER.info("Registrado creative tab de ccballs")
	}
}
