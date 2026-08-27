package com.siko.sikologinparticles;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Registra el comando /sikoeffects, restringido a operadores (nivel de
 * permiso 2), con dos subcomandos:
 * <p>
 * - /sikoeffects set <jugador> <hojas|sakura|viento|ninguno>
 * Asigna (o quita) el efecto que se reproducira la proxima vez que ese
 * jugador entre al servidor. Se guarda en disco, sobrevive a reinicios.
 * <p>
 * - /sikoeffects debug <hojas|sakura|viento>
 * Reproduce el efecto inmediatamente sobre quien ejecuta el comando, sin
 * esperar el retardo configurado y sin modificar su asignacion guardada.
 * Pensado para previsualizar como queda un efecto.
 */
public final class SikoEffectsCommand {

	private static final List<String> SET_EFFECT_NAMES =
			Arrays.asList("hojas", "sakura", "viento", "fenix", SikoEffectType.NONE_KEYWORD);
	private static final List<String> DEBUG_EFFECT_NAMES = Arrays.asList("hojas", "sakura", "viento", "fenix");

	private SikoEffectsCommand() {
	}

	public static void register(PlayerEffectStore store, AnimationManager animationManager) {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				dispatcher.register(CommandManager.literal("sikoeffects")
						.requires(source -> source.hasPermissionLevel(2))
						.then(CommandManager.literal("set")
								.then(CommandManager.argument("jugador", StringArgumentType.word())
										.suggests((context, builder) -> CommandSource.suggestMatching(
												context.getSource().getServer().getPlayerNames(), builder))
										.then(CommandManager.argument("efecto", StringArgumentType.word())
												.suggests((context, builder) -> CommandSource.suggestMatching(
														SET_EFFECT_NAMES, builder))
												.executes(context -> executeSet(context, store)))))
						.then(CommandManager.literal("debug")
								.then(CommandManager.argument("efecto", StringArgumentType.word())
										.suggests((context, builder) -> CommandSource.suggestMatching(
												DEBUG_EFFECT_NAMES, builder))
										.executes(context -> executeDebug(context, animationManager))))));
	}

	private static int executeSet(CommandContext<ServerCommandSource> context, PlayerEffectStore store) {
		String playerName = StringArgumentType.getString(context, "jugador");
		String effectId = StringArgumentType.getString(context, "efecto");

		if (effectId.equalsIgnoreCase(SikoEffectType.NONE_KEYWORD)) {
			store.setEffect(playerName, null);
			store.save();
			context.getSource().sendFeedback(
					() -> Text.literal("Efecto desactivado para " + playerName + "."), true);
			return 1;
		}

		Optional<SikoEffectType> type = SikoEffectType.fromId(effectId);
		if (type.isEmpty()) {
			context.getSource().sendError(
					Text.literal("Efecto desconocido: " + effectId + ". Usa hojas, sakura, viento, fenix o ninguno."));
			return 0;
		}

		store.setEffect(playerName, type.get());
		store.save();
		context.getSource().sendFeedback(
				() -> Text.literal("Efecto '" + type.get().getId() + "' asignado a " + playerName
						+ " para su proxima entrada al servidor."),
				true);
		return 1;
	}

	private static int executeDebug(CommandContext<ServerCommandSource> context, AnimationManager animationManager) {
		ServerPlayerEntity player;
		try {
			player = context.getSource().getPlayerOrThrow();
		} catch (CommandSyntaxException e) {
			context.getSource().sendError(Text.literal("Este comando solo se puede usar como jugador."));
			return 0;
		}

		String effectId = StringArgumentType.getString(context, "efecto");
		Optional<SikoEffectType> type = SikoEffectType.fromId(effectId);
		if (type.isEmpty()) {
			context.getSource().sendError(
					Text.literal("Efecto desconocido: " + effectId + ". Usa hojas, sakura, viento o fenix."));
			return 0;
		}

		animationManager.startImmediate(player, type.get());
		context.getSource().sendFeedback(
				() -> Text.literal("Reproduciendo efecto '" + type.get().getId() + "'..."), false);
		return 1;
	}
}
