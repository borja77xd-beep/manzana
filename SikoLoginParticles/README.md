# SikoLoginParticles

Mod para **Minecraft 1.21.1 (Fabric)** que reproduce un pequeno efecto de
particulas alrededor de un jugador cuando este entra al servidor. A
diferencia de la primera version, este mod es **100% server-side**:

- Los jugadores normales **no necesitan instalar nada** en su cliente.
- Solo hace falta instalarlo en el **servidor**, junto con Fabric API.
- El efecto lo ve **todo el mundo cerca del jugador**, no solo el.
- Por defecto **nadie tiene ningun efecto activado**. Un operador debe
  asignarselo explicitamente a cada jugador con un comando.

## Efectos disponibles (solo particulas vanilla, sin texturas propias)

| Nombre    | Que se ve                                                                 |
|-----------|----------------------------------------------------------------------------|
| `hojas`   | Remolino de hojas de roble (textura vanilla real de las hojas) llevadas por una brisa aleatoria: cada hoja nace en un punto distinto, gira un poco alrededor del cuerpo y sube de forma irregular, como si el viento la empujara de verdad. |
| `sakura`  | Petalos de flor de cerezo cayendo suavemente alrededor del jugador (particula vanilla `cherry_leaves`). |
| `viento`  | Pequeno torbellino de viento (particula vanilla del Breeze/carga de viento) que nace en los pies y sube en espiral. |
| `fenix`   | El efecto "epico", solo con fuego y brillos (sin nada de combate). Un par de alas de fuego (normal y de alma) se despliegan desde la espalda del jugador con un aleteo suave, mas una columna de fuego en el cuerpo. Empieza con un estallido de chispas doradas (particula del Totem de la Inmortalidad) y termina con un haz de luz disparandose al cielo. Dura mas que los demas (~3.5s) para que le de tiempo a desplegarse. |

## Requisitos

- Java 21
- [Fabric Loader](https://fabricmc.net/use/) compatible con 1.21.1, **instalado en el servidor**
- [Fabric API](https://modrinth.com/mod/fabric-api) compatible con 1.21.1, **instalada en el servidor**
- Minecraft 1.21.1

## Compilar el mod SIN instalar nada (recomendado si no tienes experiencia)

Este proyecto incluye un archivo (`.github/workflows/build.yml`) que hace
que **GitHub compile el mod automaticamente en la nube**, sin que tengas
que instalar Java, Gradle ni nada en tu ordenador. Pasos:

1. Crea una cuenta gratuita en https://github.com (si no tienes una).
2. Crea un repositorio nuevo: boton verde "New" -> ponle un nombre, por
   ejemplo `SikoLoginParticles` -> "Create repository".
3. Dentro del repositorio, pulsa "Add file" -> "Upload files", y sube
   **el contenido de la carpeta `SikoLoginParticles`** (todos los archivos
   y subcarpetas que hay dentro, no la carpeta contenedora en si).
   Comprueba especialmente que la carpeta `.github` se haya subido: en
   muchos sistemas, al arrastrar una carpeta al navegador, las carpetas
   que empiezan por un punto se ocultan y no se suben. Si no aparece,
   creala a mano desde GitHub: "Add file" -> "Create new file" -> escribe
   como nombre `.github/workflows/build.yml` -> pega el contenido de ese
   archivo -> "Commit changes".
4. Ve a la pestana **"Actions"**. Deberia aparecer un proceso llamado
   "Build mod" ejecutandose (tarda 2-3 minutos). Espera al check verde.
5. Entra en esa ejecucion y baja hasta **"Artifacts"**: ahi tienes un zip
   llamado `SikoLoginParticles-jar` para descargar. Dentro esta el `.jar`
   ya compilado (ignora el que lleva `-sources`, ese es solo el codigo
   fuente en texto).

## Instalar en el servidor

1. Instala Fabric Loader en tu servidor para Minecraft 1.21.1 (con el
   [Fabric Installer](https://fabricmc.net/use/installer/), eligiendo la
   opcion de servidor).
2. Descarga **Fabric API** para 1.21.1 y copiala a la carpeta `mods` del
   servidor.
3. Copia `sikologinparticles-1.0.0.jar` (el que **no** lleva `-sources`)
   a esa misma carpeta `mods` del servidor.
4. Arranca el servidor.

Los jugadores que se conecten **no necesitan instalar nada**: veran los
efectos de los demas (y los suyos propios, si se les asigna uno) de forma
totalmente normal, como si fueran particulas vanilla del juego.

## Como se usa

Todo se controla con el comando `/sikoeffects`, disponible **solo para
operadores** (nivel de permiso 2, es decir, jugadores con `/op`).

### Asignar un efecto a un jugador

```
/sikoeffects set <jugador> <hojas|sakura|viento|fenix|ninguno>
```

Ejemplos:

```
/sikoeffects set ElConfusio hojas
/sikoeffects set ElConfusio sakura
/sikoeffects set ElConfusio ninguno
```

Esto **no reproduce el efecto al momento**: queda guardado, y se
reproducira automaticamente la **proxima vez** que ese jugador entre al
servidor (y en todas las siguientes conexiones, hasta que se cambie con
`ninguno` o se le asigne otro efecto distinto). Funciona aunque el
jugador este desconectado en el momento de ejecutar el comando, y
sobrevive a reinicios del servidor (se guarda en un archivo).

### Previsualizar un efecto (comando debug)

```
/sikoeffects debug <hojas|sakura|viento|fenix>
```

Reproduce el efecto **inmediatamente sobre ti mismo**, sin esperar el
retardo configurado y sin cambiar tu asignacion guardada. Pensado para
que un operador pueda probar rapidamente como queda cada efecto antes de
asignarlo a alguien. Solo funciona ejecutado por un jugador (no desde la
consola del servidor).

## Que hace exactamente al entrar un jugador

1. Cuando un jugador con un efecto asignado termina de conectarse al
   servidor, el mod espera `delay_ticks` (5 por defecto, ~0.25s).
2. A continuacion reproduce, durante `duration_ticks` (40 = 2 segundos
   por defecto), el efecto correspondiente alrededor de su cuerpo.
3. Las particulas las genera el servidor y las ve **cualquier jugador
   cerca**, sin que nadie necesite el mod instalado.
4. Al terminar no queda ningun efecto ni entidad persistente.
5. Se ejecuta una sola vez por conexion (ver siguiente seccion).

## Deteccion de "entrada al servidor" (y por que se hizo asi)

El mod usa el evento oficial de Fabric API
`net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.JOIN`,
version server-side del mismo evento que usa el propio Minecraft para
saber cuando el `ServerPlayNetworkHandler` de un jugador esta listo para
recibir paquetes. Se dispara **una unica vez por conexion**, y no en
cambios de chunk, respawns tras morir, ni cambios de dimension dentro de
la misma conexion (portales del Nether, etc.).

**Sobre proxies tipo Velocity:** si el proxy mueve al jugador de un
servidor backend a otro mediante el paquete vanilla de "transfer"
(disponible desde 1.20.5), el jugador realiza una reconexion real a
*este* servidor backend, y el efecto se disparara con normalidad si el
mod esta instalado en el. Si en cambio tu red usa un mecanismo de cambio
de servidor sin reconexion TCP real, este mod (que ahora vive en el
backend, no en el cliente) simplemente no se entera de ese movimiento
entre backends; solo sabe de conexiones nuevas a si mismo.

## Configuracion general

Al arrancar el servidor por primera vez con el mod instalado, se crean
dos archivos:

**`config/sikologinparticles.json`** (ajustes generales, se aplican por
igual a los 3 efectos):

```json
{
  "enabled": true,
  "delayTicks": 5,
  "durationTicks": 40,
  "radius": 0.7,
  "particleCount": 50,
  "playSound": true,
  "soundVolume": 0.4,
  "soundId": "minecraft:block.amethyst_block.chime"
}
```

| Campo           | Descripcion                                                                 |
|-----------------|------------------------------------------------------------------------------|
| `enabled`       | Interruptor general: si es `false`, no se dispara ningun efecto.            |
| `delayTicks`    | Ticks de espera tras entrar al servidor antes de iniciar el efecto.         |
| `durationTicks` | Duracion total del efecto en ticks (20 ticks = 1 segundo). No aplica a `fenix`, que siempre dura al menos 70 ticks (~3.5s) para poder desplegar toda su secuencia. |
| `radius`        | Radio, en bloques, del efecto alrededor del jugador.                        |
| `particleCount` | Numero aproximado de particulas repartidas a lo largo de toda la duracion.  |
| `playSound`     | Si se reproduce un sonido corto (audible para los cercanos) al empezar.     |
| `soundVolume`   | Volumen del sonido (0.0 - 1.0 recomendado).                                 |
| `soundId`       | Identificador de un sonido vanilla.                                        |

**`config/sikologinparticles_players.json`** (que efecto tiene cada
jugador; normalmente no hace falta tocarlo a mano, se gestiona con
`/sikoeffects set`):

```json
{
  "elconfusio": "hojas"
}
```

## Estructura del proyecto

```
SikoLoginParticles/
├── build.gradle
├── settings.gradle
├── gradle.properties
├── LICENSE
├── README.md
├── .github/workflows/build.yml         (compila el mod automaticamente en GitHub)
└── src/main/
    ├── java/com/siko/sikologinparticles/
    │   ├── SikoLoginParticlesServer.java  (entrypoint + registro de eventos)
    │   ├── SikoEffectsCommand.java        (comando /sikoeffects: set y debug)
    │   ├── AnimationManager.java          (retardo + animacion activa, tick a tick)
    │   ├── EffectRenderer.java            (calculo/particulas de cada efecto)
    │   ├── SikoEffectType.java            (enum: hojas, sakura, viento)
    │   ├── PlayerEffectStore.java         (que efecto tiene cada jugador, persistente)
    │   └── ModConfig.java                 (ajustes generales, persistente)
    └── resources/
        └── fabric.mod.json
```

## Rendimiento

- No se usan hilos adicionales ni polling: toda la logica cuelga de tres
  eventos de Fabric API (`JOIN`, `DISCONNECT` y `END_SERVER_TICK`).
- Mientras no hay ningun jugador en fase de espera ni con una animacion
  activa, el tick handler no hace practicamente nada.
- Con los valores por defecto se generan ~1-2 particulas por tick durante
  2 segundos (40 ticks) por jugador con efecto activo, muy lejos de
  cualquier "explosion" de particulas.
- No se registran mixins ni paquetes de red propios; el unico coste extra
  es el comando (trivial) y el propio calculo de posiciones.
