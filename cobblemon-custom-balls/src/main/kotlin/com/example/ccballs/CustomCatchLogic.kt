package com.example.ccballs

// ======================================================================================
// PLANTILLA: logica condicional de captura para GS Ball y Escoria Ball
// ======================================================================================
// Cobblemon expone un sistema de eventos (estilo "bus" con handlers "before/after") del
// que ya se sabe, por evidencia externa, que existe un evento relacionado a la captura
// que otros addons usan para modificar el resultado (por ejemplo el mod "Catch Rate"
// que fuerza capturas garantizadas usando ese mismo hook).
//
// No incluyo aca una implementacion cerrada porque el nombre EXACTO del evento
// (posibles candidatos: CobblemonEvents.POKEMON_CAPTURED, THROWN_POKEBALL_HIT,
// CAPTURE_CALCULATED, o similar) cambia de nombre entre versiones y no quiero darte
// una linea que parezca terminada pero no compile.
//
// PASOS para completar esto vos mismo (10-15 min):
//   1. En tu IDE (IntelliJ recomendado), con el proyecto ya sincronizado (gradlew build
//      o el import de Gradle), busca la clase "CobblemonEvents" con Ctrl+N / Cmd+O.
//   2. Adentro vas a ver una lista de "val ALGO_EVENT = ..." - busca los que tengan
//      "capture", "catch" o "pokeball" en el nombre.
//   3. Cada evento normalmente se subscribe asi:
//         CobblemonEvents.NOMBRE_DEL_EVENTO.subscribe(Priority.NORMAL) { event ->
//             // tu logica aca, usando event.pokeBall, event.pokemon, event.player, etc.
//         }
//   4. Para la GS Ball: revisa si el evento te da acceso al Pokemon objetivo y a su
//      "species.labels" o similar para chequear si tiene el label "legendary"/"mythical".
//      Si es asi, subis el multiplicador de captura (o forzas exito) SOLO cuando
//      pokeBall == CustomPokeBalls.GS_BALL.
//   5. Para la Escoria Ball: misma idea pero para bajar el multiplicador o, si queres
//      que directamente falle salvo excepciones, cancelar/forzar fallo del evento
//      cuando pokeBall == CustomPokeBalls.ESCORIA_BALL.
//
// Registra la subscripcion llamando a CustomCatchLogic.register() desde
// CustomCobblemonBalls.onInitialize() una vez la tengas escrita.
// ======================================================================================

object CustomCatchLogic {

	fun register() {
		// TODO: completar subscripciones a eventos de Cobblemon aca (ver comentario arriba)
		CustomCobblemonBalls.LOGGER.info("CustomCatchLogic listo para completar (ver comentarios en el archivo)")
	}
}
