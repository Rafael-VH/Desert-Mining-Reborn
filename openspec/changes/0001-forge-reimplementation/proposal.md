# Proposal — Clean-room reimplementation on Forge 1.20.1

**Change ID**: `0001-forge-reimplementation`
**Status**: draft (bloqueado por 1 decisión: package root)
**Base**: `openspec/project.md`

## Why

**Desert Mining 1.0.0** solo existe para Forge 1.20.1, su código fuente no es público y el JAR
desplegado tiene defectos técnicos conocidos (procedimientos generados por MCreator, `Math.random()`
en lugar de `RandomSource`, worldgen que sobrescribe arena a cualquier altura 0–255, sin config).

Este cambio construye un mod equivalente desde cero: misma funcionalidad observable, código propio,
arquitectura idiomática de Forge y configurable. `recursos/` se usa **solo como referencia**.

## What Changes

Se construye el proyecto completo (hoy el repo solo tiene documentación):

1. Bootstrap ForgeGradle 6 para 1.20.1 con build en verde.
2. 5 bloques de mena de arena + `BlockItem`, con `mineable/shovel`.
3. 5 items de fragmento de arena.
4. 5 tamices manuales con durabilidad escalonada y consumo vía crafteo.
5. 3 palas (fortunate, speedy, master) + 3 items de mejora aplicables por smithing.
6. Recetas de procesado chunk + tamiz → recurso vanilla.
7. Worldgen de las 5 menas en bioma desierto.
8. Loot tables, recetas de crafteo y tags.
9. Config `desert_mining_reborn-common.toml`.
10. Assets propios provistos por el usuario.

## Fuera de alcance

- Port a 1.21+ o NeoForge.
- Soporte Fabric / multi-loader.
- Cualquier asset o nombre del mod original.
- Publicación en CurseForge / Modrinth.

## Decisión abierta

| ID | Decisión | Impacto |
| :--- | :--- | :--- |
| **D1** | ~~Package root~~ **RESUELTO**: `com.rexthar.desertminingreborn` | — |

## Fases y tareas

### Fase A — Bootstrap (desbloqueada)

- **T1** Fijar D1 y actualizar `openspec/project.md`.
- **T2** `gradle.properties`: `mod_id=desert_mining_reborn`, versión, `forge_version=47.4.28`, `mapping_channel=official`.
- **T3** `build.gradle` con ForgeGradle 6, toolchain Java 17, `mappings channel: 'official', version: '1.20.1'`.
- **T4** `settings.gradle` + wrapper de Gradle.
- **T5** `META-INF/mods.toml` con `modId = desert_mining_reborn`, metadata y dependencia de Forge.
- **T6** `pack.mcmeta` con `pack_format: 15` (1.20.1).
- **T7** Completar `.gitignore`: `/run/`, `/build/`, `.gradle/`, `*.jar`, `!gradle/wrapper/gradle-wrapper.jar`.
- **T8** Clase principal `DesertMiningReborn` con `@Mod` y `DeferredRegister` de bloques, items y creative tab.
- **T9** Gate: `./gradlew build` verde sobre el template vacío.

### Fase B — Registro de contenido (sin lógica)

- **T10** 5 bloques `SandOreBlock` con dureza, resistencia y `requiresCorrectToolForDrops`.
- **T11** 5 `BlockItem` + creative tab propia con todo el contenido.
- **T12** 5 items `SandyChunk`.
- **T13** 5 items de tamiz con `Properties().durability(4/32/128/512/2048)` y `getCraftingRemainingItem` que devuelve el tamiz con +1 daño o vacío.
- **T14** Item tag `desert_mining_reborn:hand_sieves` (`data/<ns>/tags/items/`).
- **T15** Block tag `minecraft:mineable/shovel` (`data/minecraft/tags/blocks/mineable/shovel.json`).
- **T16** 3 palas `ShovelItem` con tier propio + 3 items de mejora (`stacksTo(64)`), sin lógica aún. Valores verificados: Fortunate 2031/9.0, Speedy 2031/12.0, Master 4062/15.0; todas con `attackDamageBonus 4.0`, `getLevel() 3`, `enchantmentValue 2`.
- **T17** Gate: `./gradlew build` verde + verificación in-game de que todo aparece.

### Fase C — Lógica (Strict TDD)

Diseño clave: la matemática de probabilidades vive en clases **Java puras** (sin imports de
Minecraft) para poder testear con JUnit sin arrancar el juego.

- **T18** `SieveTier` enum puro: durabilidad (4/32/128/512/2048) y recurso resultante. RED → GREEN.
- **T19** `DropMath` puro con las dos funciones reales:
  `fortunateBonusCount(random, fortuneLevel) = floor(random * (2 + fortuneLevel))` y
  `masterBonusCount(random, fortuneLevel) = floor(random * (3 + fortuneLevel))`, ambas tras un
  gate de 50 %. Tests de distribución, bordes (fortune 0 y 3) y determinismo con `RandomSource` sembrado.
- **T20** Tabla de probabilidades de tamizado (`SieveYieldTable`), pura y testeable.
- **T21** Hook de la Fortunate: override `IForgeItem#mineBlock` que delega en `DropMath`. Test de la función de probabilidad.
- **T22** Hook de la Speedy: sin lógica propia — la velocidad sale del tier (12.0). Solo test de que el tier reporta lo esperado.
- **T23** Hook de la Master: `mineBlock` con `DropMath.masterBonusCount`.
- **T24** Gate: `./gradlew test` y `./gradlew build` verdes.

**Nota de diseño (verificada)**: "romper arena al instante" **no se implementa**. Arena tiene dureza 0.5 y multiplicador 1.5 de herramienta correcta; `0.5 × 1.5 ÷ 15.0 = 0.05 s` = 1 tick, el mínimo posible. El comportamiento emerge del `getSpeed()` del tier. Cualquier hack de instant-break sería un defecto.

### Fase D — Worldgen

- **T25** 5 `configured_feature` (`minecraft:ore`, `size: 4`, target `minecraft:sand`, `discard_chance_on_air_exposure: 0`).
- **T26** 5 `placed_feature` (`count`, `in_square`, `height_range`, `biome`) con altura razonable — **no** 0–255 como el original.
- **T27** 5 `forge:biome_modifier` (`forge:add_features`, `biomes: "desert"`, step `underground_ores`).
- **T28** Gate: generar chunks de desierto y verificar aparición.

### Fase E — Datos

- **T29** 5 loot tables de bloque: 1 chunk sin Silk Touch, bloque con Silk Touch, y `apply_bonus` con `formula: ore_drops` para que **Fortune multiplique los chunks**.
- **T30** Recetas de los 5 tamices (shaped + smithing para enhanced).
- **T31** Recetas de las 3 mejoras (`aaa/bcb/aaa`) y de las 3 palas (`smithing_transform` sobre `netherite_shovel`).
- **T32** 5 recetas `chunk + #hand_sieves` → recurso vanilla.
- **T33** Gate: verificación in-game de drops y crafteos.

### Fase F — Configuración

- **T34** `ForgeConfigSpec` con secciones `worldgen`, `sieve`, `shovels`, `processing`.
- **T35** Registrar la config en el constructor del mod y aplicarla en worldgen, loot y lógica.
- **T36** Test de carga y defaults.

### Fase G — Assets (requiere aprobación previa)

- **T37** Aprobar la lista final de menas, tamices y palas.
- **T38** Integrar las texturas 16×16 provistas por el usuario.
- **T39** Blockstates, modelos de bloque y de item.
- **T40** `lang/en_us.json` y `lang/es_es.json` con nombres propios.
- **T41** Gate: verificación visual in-game.

### Fase H — Cierre

- **T42** `./gradlew build` final; verificar que el `.jar` no contiene nada de `recursos/`.
- **T43** Prueba en cliente y servidor dedicado.
- **T44** `README.md` real + `LICENSE`.
- **T45** Auditoría clean-room: comparar comportamiento, no implementación.

## Estimación de carga de revisión

| Métrica | Valor |
| :--- | :--- |
| Líneas cambiadas estimadas | ~1.800 – 2.600 (código + JSON + assets) |
| Presupuesto configurado | 800 líneas |
| Estrategia | `auto-forecast` |
| Chained PRs recomendado | **Sí** |

**Cortes propuestos**:

1. **PR #1** — Fase A + B (bootstrap y registro). Habilita todo lo demás.
2. **PR #2** — Fase C (lógica con tests).
3. **PR #3** — Fase D + E (worldgen y datos).
4. **PR #4** — Fase F (config).
5. **PR #5** — Fase G + H (assets y cierre).

## Riesgos

| Riesgo | Prob. | Mitigación |
| :--- | :--- | :--- |
| Contaminación clean-room (el descompilado está a un `read` de distancia) | **alta** | Nunca abrir `salida-*/` mientras se escribe código; auditar en T45 |
| `smithing_transform` con `template: []` se comporta distinto en 1.20.1 | media | Probar en T31; si falla, usar `crafting_shaped` |
| Datos de item en NBT (no Data Components) provoca fugas de estado | media | Encapsular lectura/escritura en helpers |
| Unit tests que tocan clases de Minecraft sin runtime | media | Aislar la matemática en clases Java puras (T18–T20) |
| Worldgen 0–255 sobrescribe arena en superficie y cuevas | alta | Corregir el rango en T26 y documentar el desvío |
| Implementar un hack de "instant break" que el original no necesita | media | T24: es emergente del tier 15.0; no escribir código especial |
| Doble bonificación por Fortuna confundida como bug | media | Es intencional: loot table `ore_drops` **más** el bono de la pala. Documentar en el tooltip |
| Assets bloqueados por falta de aprobación | alta | Fase G al final, con gate explícito |

## Criterios de aceptación

- `./gradlew build` verde en cada fase.
- `./gradlew test` verde con tests de la matemática de tamices y palas.
- Las 5 menas aparecen en desierto y se minan con pala.
- El tamiz consume durabilidad y devuelve el recurso correcto por tier.
- Las 3 palas modifican drops y velocidad según especificación.
- El `.jar` final no contiene ningún rastro de `recursos/` ni del original.
