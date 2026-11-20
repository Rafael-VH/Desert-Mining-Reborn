# 07 — Desarrollo

Guía para quien va a **construir el mod**, **tocar el código** o **crear contenido compatible**
(datapacks, addons, otros mods).

Si solo quieres jugar, este no es tu documento: vuelve al [`README.md`](../README.md).

Convención de marcas: ver [`README.md`](README.md).

---

## 1. Stack

| Componente | Valor |
| :--- | :--- |
| Minecraft | **1.20.1** |
| Forge | **47.4.28** |
| Java | **17** |
| Build | **ForgeGradle 6** sobre el MDK oficial |
| Mappings | **official** (Mojang) |
| Metadata | `src/main/resources/META-INF/mods.toml` |
| Datos de items | **NBT** |

**No** es NeoForge. **No** es 1.21. La mitad de los tutoriales que encuentres por internet no aplican.

### Lo que no existe en 1.20.1

Si vienes de una versión moderna, olvídate de esto:

| No existe | En su lugar |
| :--- | :--- |
| `Data Components` | NBT (`ItemStack.getOrCreateTag()`) |
| `MapCodec` | `Codec` / `RecordCodecBuilder` |
| `neoforge.mods.toml` | `META-INF/mods.toml` |
| `ModDevGradle` | ForgeGradle 6 |
| Carpetas de datos en singular | Van en **plural**: `recipes/`, `loot_tables/`, `tags/blocks/` |
| Craftera automática | — (llegó en 1.21) |

---

## 2. Puesta en marcha

> **Estado:** el proyecto Gradle todavía no existe. La Fase A de
> [`06-plan-implementacion.md`](06-plan-implementacion.md) lo crea. Hasta entonces estos comandos
> no funcionan.

```bash
./gradlew build            # compilar y empaquetar
./gradlew test             # solo tests unitarios (rápido, sin arrancar el juego)
./gradlew runClient        # cliente de desarrollo
./gradlew runServer        # servidor de desarrollo
./gradlew --refresh-dependencies
./gradlew clean
```

### Requisitos del entorno

- **JDK 17** en el `PATH` (o configurado en el toolchain de Gradle).
- IntelliJ IDEA para trabajar cómodo con los mappings.
- **Nunca** subir `run/`, `build/`, `.gradle/` ni `recursos/`.

---

## 3. Estructura del repositorio

```
.
├── AGENTS.md              Reglas del proyecto para agentes de IA
├── README.md              Entrada para jugadores
├── docs/                  Documentación funcional y de desarrollo
├── openspec/              Contexto SDD y proposal del cambio
├── recursos/              Material de referencia — IGNORADO POR GIT
└── src/                   Código del mod (aún no existe)
```

`recursos/` contiene el JAR original, las extracciones y el descompilado. **Es solo material de
análisis.** Nunca se importa desde ahí al proyecto.

---

## 4. Estructura del código

```
src/main/java/com/rexthar/desertminingreborn/
├── DesertMiningReborn.java
├── registry/
│   ├── ModBlocks.java
│   ├── ModItems.java
│   └── ModCreativeTabs.java
├── block/
│   └── SandyOreBlock.java           una clase, cinco instancias
├── item/
│   ├── SandyChunkItem.java
│   ├── HandSieveItem.java
│   ├── ModShovelTier.java
│   └── UpgradeItem.java
├── worldgen/
│   └── SandyOreBiomeModifier.java
├── config/
│   └── DesertMiningRebornConfig.java
└── logic/                            Java puro, sin imports de Minecraft
    ├── DropMath.java
    ├── SieveTier.java
    └── SieveRules.java

src/main/resources/
├── assets/desert_mining_reborn/
├── data/desert_mining_reborn/
├── data/minecraft/tags/blocks/mineable/shovel.json
└── META-INF/mods.toml
```

### Dos decisiones de diseño

**Clases parametrizadas, no una por item.** El original tiene 21 clases casi idénticas porque se
generó con MCreator. Aquí `SandyOreBlock` recibe sus parámetros por constructor y se registra cinco
veces. Mismo resultado, una fracción del código.

**La matemática no importa Minecraft.** `logic/` no tiene ni un `import net.minecraft.*`. Eso permite
que `./gradlew test` corra en segundos sin arrancar el juego. Si metes un `ItemStack` ahí, los tests
se vuelven inútiles y nadie los corre.

---

## 5. Convenciones

- Paquete raíz: `com.rexthar.desertminingreborn`.
- Namespace: `desert_mining_reborn:` — **nunca** `desert_mining:`.
- Clases `PascalCase`, métodos y variables `camelCase`, constantes `SCREAMING_SNAKE_CASE`.
- `@Override` explícito siempre.
- Javadoc en toda clase pública.
- Java 17: records, sealed classes, pattern matching.
- Sin código comentado sin justificar. Sin TODOs sin contexto.

---

## 6. Reglas clean-room (no negociables)

1. **Nunca** copiar código del original, ni parcialmente.
2. **Nunca** extraer texturas, modelos, sonidos ni datos del JAR original.
3. **Nunca** subir a Git el JAR, el descompilado ni capturas de él.
4. Los nombres de clases, bloques e items deben ser propios.
5. Se puede consultar la **funcionalidad**; la **implementación** es original.

### La regla práctica

**No abras `recursos/vineflower-desert/` ni `recursos/cfr-desert/` mientras escribes código.**

Consulta antes, documenta el comportamiento en `docs/`, cierra el archivo y recién entonces
implementa desde `docs/`. Eso es lo que hace que el clean room sea real y no una declaración de
intenciones.

---

## 7. Trampas de 1.20.1

### El mod id no admite guiones

Forge valida el id con `^[a-z][a-z0-9_]{1,63}$`. Un guion **no pasa**. Por eso el namespace es
`desert_mining_reborn` y no `desert-mining-reborn`. Afecta a:

- `mods.toml` (`modId`)
- `assets/<ns>/` y `data/<ns>/`
- el prefijo de todos los `ResourceLocation`

El fallo aparece al **arrancar**, no al compilar. Si el mod no carga y no hay error de build, revisa
esto primero.

### El nombre del archivo de config no se elige

Forge lo deriva del mod id. Con `mod_id=desert_mining_reborn` el archivo es
`config/desert_mining_reborn-common.toml`. No lo hardcodees en la documentación como si fuera
configurable.

### Los JSON de biome modifier no leen la config

`forge:biome_modifier` es un datapack JSON: no puede consultar el TOML. Si quieres que la config
apague minerales de verdad, el `BiomeModifier` va **en Java**. Ver
[`04-worldgen.md`](04-worldgen.md) §4.2.

### Las menas heredan de `FallingBlock`

No es un detalle estético. Cambia el comportamiento de minado, de explosiones y de derrumbe. Está
documentado en [`02-interacciones.md`](02-interacciones.md) §1.3 y la decisión D3 sigue abierta.

### "Romper arena al instante" no es código

Sale de la velocidad del tier: `0.5 × 1.5 ÷ 15.0 = 0.05 s`. Si ves un `if` que detecta arena y llama
a `destroyBlock`, es un defecto.

---

## 8. Testing

| Nivel | Qué cubre | Herramienta |
| :--- | :--- | :--- |
| Unitario puro | `DropMath`, `SieveTier`, config | JUnit 5, `./gradlew test` |
| GameTest | worldgen, drops, gravedad | Forge GameTest |
| In-game | recetas, crafteo, balance | manual |

Los tests de la Fase C son **TDD estricto**: se escribe el test, se ve fallar, se implementa, se ve
pasar. No hay TDD para assets ni para worldgen visual.

---

## 9. Estrategia de PRs

El cambio completo ronda las 1.800–2.600 líneas, muy por encima de un presupuesto de revisión
razonable. Los cortes:

| PR | Contenido | Fases |
| :--- | :--- | :--- |
| #1 | Bootstrap y registro | A + B |
| #2 | Lógica con tests | C |
| #3 | Worldgen y datos | D + E |
| #4 | Configuración | F |
| #5 | Assets y cierre | G + H |

Cada PR debe compilar y dejar el mod jugable al nivel de su fase.

Antes de commitear, carga la skill `git-commit`. Un commit por unidad lógica, Conventional Commits,
sin atribución de IA.

---

## 10. Para creadores de contenido y otros mods

Puntos de extensión, para que puedas integrarte sin pelear con el código:

### Tags

| Tag | Qué controla |
| :--- | :--- |
| `minecraft:mineable/shovel` | palas válidas para las menas (entrada **aditiva**, `replace: false`) |
| `desert_mining_reborn:hand_sieves` | qué items cuentan como tamiz en las recetas de tamizado |

Añadir tu pala a `minecraft:mineable/shovel` la hace válida para minar las menas, sin tocar nada
más. Añadir tu item al tag de tamices lo hace usable en las recetas.

### Worldgen

El `BiomeModifier` se implementa en Java y lee la config. La propuesta es exponer un tag de bioma
propio para que los datapacks puedan ampliar dónde aparecen las menas sin editar el mod. Ver
[`04-worldgen.md`](04-worldgen.md) §4.1.

### Config

`config/desert_mining_reborn-common.toml` permite apagar minerales individualmente y ajustar rangos
Y, durabilidades y probabilidades. Es la vía correcta para convivir con otro mod de worldgen que
también toque el desierto.

Detalle completo en [`05-configuracion.md`](05-configuracion.md).

---

## 11. Estado actual

| Ítem | Estado |
| :--- | :--- |
| Análisis funcional | completo |
| Documentación | completa |
| Plan de implementación | escrito |
| Proyecto Gradle | **no existe** |
| Código | **no existe** |
| Assets | pendientes de aprobación |

Siguiente paso: **Fase A** de [`06-plan-implementacion.md`](06-plan-implementacion.md).

Decisiones todavía abiertas:

| ID | Decisión | Bloquea |
| :--- | :--- | :--- |
| **D2** | Assets: origen y aprobación | Fase G |
| **D3** | ¿Las menas conservan la gravedad? | Fase B |
| **D4** | Rango Y del worldgen (0–255 fiel vs por capas) | Fase D |
