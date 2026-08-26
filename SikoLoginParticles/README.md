# SikoLoginParticles

Mod muy simple para **Minecraft 1.21.1 (Fabric)** que reproduce una pequena
animacion de particulas en espiral ascendente alrededor del jugador cuando
este entra a un mundo o servidor. El mod es **100% client-side**: no
necesita instalarse en el servidor (funciona en servidores vanilla o
modded sin ningun requisito adicional).

## Requisitos

- Java 21
- [Fabric Loader](https://fabricmc.net/use/) compatible con 1.21.1
- [Fabric API](https://modrinth.com/mod/fabric-api) compatible con 1.21.1
- Minecraft 1.21.1

## Compilar el mod SIN instalar nada (recomendado si no tienes experiencia)

Este proyecto incluye un archivo (`.github/workflows/build.yml`) que hace
que **GitHub compile el mod automaticamente en la nube**, sin que tengas
que instalar Java, Gradle ni nada en tu ordenador. Pasos:

1. Crea una cuenta gratuita en https://github.com (si no tienes una).
2. Crea un repositorio nuevo: boton verde "New" -> ponle un nombre, por
   ejemplo `SikoLoginParticles` -> "Create repository". Puede ser privado
   o publico, da igual.
3. Dentro del repositorio recien creado, pulsa "uploading an existing
   file" (o "Add file" -> "Upload files").
4. Arrastra **toda la carpeta `SikoLoginParticles`** (la que has
   descomprimido del zip) a esa pagina. GitHub subira todos los archivos
   respetando las carpetas. Pulsa "Commit changes".
5. Ve a la pestana **"Actions"** en la parte superior del repositorio.
   Veras un proceso llamado "Build mod" ejecutandose (tarda 2-3 minutos).
   Espera a que aparezca un check verde.
6. Haz clic en esa ejecucion y baja hasta la seccion **"Artifacts"**. Ahi
   encontraras un archivo `SikoLoginParticles-jar.zip` para descargar:
   dentro esta tu `.jar` ya compilado, listo para poner en la carpeta
   `mods` de Minecraft.

Si en el futuro cambias algo del codigo o de la configuracion y vuelves a
subir los archivos, GitHub volvera a compilar automaticamente.

## Compilar el mod en tu propio ordenador (alternativa)

Si prefieres compilarlo tu mismo en local, este repositorio incluye el
proyecto Gradle completo, pero **no incluye el binario
`gradle-wrapper.jar`** (no se puede generar un `.jar` binario valido como
texto). Antes de compilar por primera vez necesitas generarlo una vez, con
cualquiera de estas dos opciones:

**Opcion A - Tienes Gradle instalado en tu sistema:**

```bash
gradle wrapper --gradle-version 8.8
```

Ejecuta este comando dentro de la carpeta del proyecto. Esto creara
`gradlew`, `gradlew.bat` y `gradle/wrapper/gradle-wrapper.jar`.

**Opcion B - Usas IntelliJ IDEA:**

Simplemente abre la carpeta del proyecto como proyecto Gradle
(`File -> Open`). IntelliJ detectara el `build.gradle` y generara el
wrapper automaticamente al sincronizar.

Una vez tengas el wrapper, compila normalmente:

```bash
./gradlew build
```

El `.jar` resultante aparecera en `build/libs/sikologinparticles-1.0.0.jar`.

> Nota sobre versiones: `gradle.properties` fija versiones concretas de
> Yarn, Fabric Loader y Fabric API compatibles con 1.21.1. Si al compilar
> Gradle no encuentra alguna de ellas (porque haya sido reemplazada por una
> mas reciente), consulta las versiones actuales en
> https://fabricmc.net/develop y actualiza `gradle.properties`.

## Instalar

1. Instala Fabric Loader para Minecraft 1.21.1.
2. Copia `fabric-api-*.jar` (Fabric API) y `sikologinparticles-*.jar` (este
   mod) a la carpeta `mods` de tu instalacion de Minecraft.
3. Lanza el juego con el perfil de Fabric.

## Que hace exactamente

1. Cuando el cliente termina de conectarse a un mundo/servidor, el mod
   espera `delay_ticks` (5 por defecto).
2. A continuacion reproduce, durante `duration_ticks` (40 = 2 segundos por
   defecto), una espiral de particulas que nace junto a los pies del
   jugador y asciende girando alrededor de su cuerpo.
3. Al terminar, no queda ningun efecto ni entidad persistente: las
   particulas vanilla se generan y se apagan solas, y el mod simplemente
   deja de generar nuevas.
4. Opcionalmente reproduce un sonido corto al empezar la animacion.

La posicion de cada particula se calcula cada tick con trigonometria simple
(coordenadas polares que suben con el tiempo), no hay ninguna tabla de
posiciones precalculada ni assets externos.

## Deteccion de "entrada al mundo" (y por que se hizo asi)

El mod usa el evento oficial de Fabric API
`net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.JOIN`,
que se dispara **una unica vez por conexion**, cuando el
`ClientPlayNetworkHandler` del cliente queda listo. Esto cubre:

- Entrar a un mundo en un jugador (singleplayer).
- Conectarse a un servidor dedicado.
- Reconectarse a traves de un proxy (Velocity, etc.) cuando este realiza el
  cambio mediante el paquete vanilla de "transfer" (disponible desde la
  1.20.5), ya que en ese caso el cliente abre una conexion nueva de verdad.

Y **no** se dispara en:

- Cambios de chunk.
- Respawns tras morir.
- Cambios de dimension dentro de la misma conexion (por ejemplo, un portal
  del Nether).

Esto es exactamente lo que pide la especificacion (ejecutar una sola vez
por conexion, sin repeticiones en cada respawn o carga de chunk).

**Limitacion honesta:** muchas redes basadas en Velocity cambian al jugador
de servidor backend **sin** abrir una nueva conexion TCP; en ese caso, el
cliente recibe un paquete de "respawn"/cambio de dimension identico, a
nivel de protocolo, al de un respawn normal o un viaje por portal. No hay
forma fiable de distinguir ambos casos solo desde el cliente y sin logica
de red propia (que esta explicitamente descartada en los requisitos).
Intentar adivinarlo disparando la animacion en cualquier cambio de
dimension rompe el requisito de "no repetir en cada respawn". Por eso el
mod se limita, de forma deliberada, a `ClientPlayConnectionEvents.JOIN`,
que es la unica senal que se puede verificar con fiabilidad total desde el
cliente. Si tu proxy usa el mecanismo moderno de "transfer", el
comportamiento pedido en el punto 9 de la especificacion funciona sin nada
adicional.

## Configuracion

Al arrancar el juego por primera vez con el mod instalado, se crea el
archivo `config/sikologinparticles.json`:

```json
{
  "enabled": true,
  "delayTicks": 5,
  "durationTicks": 40,
  "radius": 0.7,
  "particleCount": 50,
  "particleType": "minecraft:end_rod",
  "playSound": true,
  "soundVolume": 0.4,
  "soundId": "minecraft:block.amethyst_block.chime"
}
```

| Campo           | Descripcion                                                                 |
|-----------------|------------------------------------------------------------------------------|
| `enabled`       | Activa/desactiva el mod por completo.                                       |
| `delayTicks`    | Ticks de espera tras entrar al mundo antes de iniciar la animacion.         |
| `durationTicks` | Duracion total de la animacion en ticks (20 ticks = 1 segundo).             |
| `radius`        | Radio, en bloques, de la espiral alrededor del jugador.                    |
| `particleCount` | Numero aproximado de particulas repartidas a lo largo de toda la animacion.|
| `particleType`  | Identificador de una particula vanilla **simple** (sin parametros extra).  |
| `playSound`     | Si se reproduce un sonido corto al iniciar la animacion.                    |
| `soundVolume`   | Volumen del sonido (0.0 - 1.0 recomendado).                                 |
| `soundId`       | Identificador de un sonido vanilla.                                        |

`particleType` acepta cualquier particula vanilla "simple" registrada
(las que no requieren parametros extra), por ejemplo:
`minecraft:end_rod`, `minecraft:enchant`, `minecraft:happy_villager`,
`minecraft:crit`, `minecraft:witch`, `minecraft:composter`. Si pones un
identificador invalido o de una particula parametrizada (como
`minecraft:dust`, que necesita color), el mod usara automaticamente
`minecraft:end_rod` como respaldo.

## Estructura del proyecto

```
SikoLoginParticles/
├── build.gradle
├── settings.gradle
├── gradle.properties
├── LICENSE
├── README.md
└── src/main/
    ├── java/com/siko/sikologinparticles/
    │   ├── SikoLoginParticlesClient.java   (entrypoint + registro de eventos)
    │   ├── LoginParticleAnimator.java      (calculo de la espiral, tick a tick)
    │   └── ModConfig.java                  (carga/guardado de config JSON)
    └── resources/
        └── fabric.mod.json
```

## Rendimiento

- No se usan hilos adicionales ni polling: toda la logica cuelga de dos
  eventos de Fabric API (`JOIN` y `END_CLIENT_TICK`).
- Mientras no hay animacion activa, el tick handler solo hace un par de
  comparaciones enteras.
- Con los valores por defecto se generan ~1-2 particulas por tick durante 2
  segundos (40 ticks), muy lejos de cualquier "explosion" de particulas.
- No se registran mixins, comandos, GUIs ni paquetes de red propios.
