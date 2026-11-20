# Project Context — Desert Mining Reborn

## Purpose

**Reimplementación desde cero** del mod **Desert Mining** (Forge 1.20.1, versión 1.0.0) sobre
**Forge 1.20.1**, con enfoque *clean room*: se estudia la **funcionalidad** del JAR original y se
reescribe con código propio.

No es un port de versión: el target es la misma versión y el mismo loader que el original. El
objetivo es un mod funcionalmente equivalente, técnicamente mejor y con assets propios.

**Prohibido de forma absoluta**: copiar código, texturas, modelos, sonidos, nombres únicos ni la
estructura de clases del original. `recursos/` es material de **referencia**, nunca fuente de assets.

## Stack

| Componente | Valor |
| :--- | :--- |
| Minecraft | **1.20.1** |
| Loader | **Forge 47.x** (47.4.28 disponible en `recursos/`) |
| Java | **17** |
| Build | **ForgeGradle 6** |
| Mappings | Oficiales de Mojang |
| Metadata | `META-INF/mods.toml` (Forge, no NeoForge) |
| Datos de item | **NBT** (`ItemStack` tag) — los Data Components son de 1.20.5+ |
| Namespace | `desert_mining_reborn` |
| Config | `config/desert-mining-reborn.toml` (`ForgeConfigSpec`) |
| Package root | `com.rexthar.desertminingreborn` — **definido** |

## Estado actual del repositorio (verificado)

- Git en `main`, un único commit (`c92f79c Initial commit`).
- Versionado: `AGENTS.md`, `README.md` (**stub de una línea**), `.gitignore`, `openspec/`.
- **No existe** `build.gradle`, `settings.gradle`, `gradle.properties`, `gradlew`, `src/`, `run/`, `build/`. El proyecto **no compila**.
- `.gitignore` ignora `recursos/` pero **le faltan** `/run/`, `/build/`, `.gradle/`, `*.jar` (con `!gradle/wrapper/gradle-wrapper.jar`).
- `.obsidian/` sin trackear (vault de notas dentro del repo).

## Material de referencia local — `recursos/` (gitignored)

Rutas actuales tras la reorganización del usuario:

| Ruta | Contenido |
| :--- | :--- |
| `recursos/Desert Mining 1.0.0 -1.20.1.jar` | JAR original |
| `recursos/Desert Mining 1.0.0 -1.20.1/` | JAR extraído: `assets/`, `data/`, `META-INF/`, `net/` |
| `recursos/vineflower-desert/` | Descompilación Vineflower (código + `data/` legible) |
| `recursos/cfr-desert/` | Descompilación CFR |
| `recursos/forge-1.20.1-47.4.28-installer.jar` | Instalador de Forge, versión a fijar |
| `recursos/vineflower-1.12.0.jar`, `recursos/cfr-0.152.jar` | Descompiladores |

**Uso permitido**: leer para entender comportamiento, valores numéricos y formato de datos de 1.20.1.
**Uso prohibido**: copiar archivos, assets o código al proyecto.

## Matriz funcional verificada

### Menas — 5
`sandy_gold_ore`, `sandy_redstone_ore`, `sandy_lapis_ore`, `sandy_diamond_ore`, `sandy_emerald_ore`.

- Bloque con tag `minecraft:mineable/shovel`. Requiere pala para drop.
- Worldgen: **ore que reemplaza `minecraft:sand`** (no sandstone), `size: 4`,
  `discard_chance_on_air_exposure: 0`, `count: 4` por chunk, `in_square`,
  altura **absoluta 0–255**, bioma `desert`, step `underground_ores`.

### Loot tables (comportamiento verificado)
- Sin Silk Touch: drop de **1 chunk** del tipo correspondiente.
- Con **Fortune**: `apply_bonus` con `formula: ore_drops` → **la Fortuna multiplica los chunks**.
- Con Silk Touch: drop del **bloque de mena** en sí.

Esto es lo que la descripción de CurseForge llama "afectados por el encantamiento Fortuna".

### Fragmentos — 5
`sandy_*_chunk`, uno por mena.

### Tamices — 5 (durabilidad y recetas verificadas)

| Tamiz | Durabilidad | Receta |
| :--- | ---: | :--- |
| Sandy | 4 | shaped `aba/b_b/aba` — a: `#minecraft:sandstone`, b: stick |
| Reinforced | 32 | shaped `aba/b_b/aba` — a: `iron_ingot`, b: `string` |
| Advanced | 128 | shaped `aba/b_b/aba` — a: `gold_ingot`, b: `string` |
| Diamond-Tipped | 512 | smithing: `advanced_hand_sieve` + `diamond`, sin template |
| Enhanced (netherita) | 2048 | smithing: `diamond_tipped_hand_sieve` + `netherite_ingot`, sin template |

Los tres primeros se craftean desde cero; los dos últimos se forjan. **No** son una cadena de
upgrades consecutivos.

Mecánica: el tamiz **no se usa sobre un bloque**. Es el ingrediente de un `crafting_shapeless`
(`chunk + #desert_mining:hand_sieves` → recurso vanilla) y `getCraftingRemainingItem` devuelve el
tamiz con 1 punto de daño, destruyéndolo al alcanzar el máximo.

### Recursos resultantes del tamizado
`minecraft:diamond`, `minecraft:emerald`, `minecraft:gold_ingot`, `minecraft:lapis_lazuli`,
`minecraft:redstone`.

### Palas — 3 (valores de tier verificados)

Todas usan `getLevel() = 3` (nivel diamante), `getAttackDamageBonus() = 4.0`,
`getEnchantmentValue() = 2`.

| Pala | Durabilidad | Velocidad | Bono de drops |
| :--- | ---: | ---: | :--- |
| Fortunate | 2031 | 9.0 | 50 % → `floor(random * (2 + fortuneLevel))` chunks extra |
| Speedy | 2031 | 12.0 | — |
| Master | 4062 | 15.0 | 50 % → `floor(random * (3 + fortuneLevel))` chunks extra |

**Romper arena al instante es emergente, no código especial.**
Arena: dureza 0.5, multiplicador de herramienta correcta 1.5.
`0.5 × 1.5 ÷ 15.0 = 0.05 s` = 1 tick. Solo la Master alcanza el mínimo de 1 tick.
Speedy (12.0) da 0.0625 s → 2 ticks; Fortunate (9.0) da 0.083 s → 2 ticks.

Conclusión de diseño: **no implementar un hack de "instant break"**. Basta con `getSpeed() = 15.0`.

### Mejoras de pala — 3
Items apilables a 64, sin comportamiento propio, aplicados con **`smithing_transform` sobre una
`netherite_shovel`** vanilla (sin template).

`fortunate_shovel_upgrade` se craftea `crafting_shaped` `aaa/bcb/aaa`
(a: `diamond`, b: `diamond_block`, c: `sand`). Las otras dos mejoras siguen el mismo patrón con
otro material central.

### Tags de datos 1.20.1 (formato plural, verificado)
- `data/minecraft/tags/blocks/mineable/shovel.json`
- `data/<ns>/tags/items/hand_sieves.json`

## Convenciones

- PascalCase clases, camelCase métodos/variables, SCREAMING_SNAKE_CASE constantes.
- Javadoc en toda clase pública. `@Override` explícito. Sin código comentado sin justificación. Sin TODO sin contexto.
- Registro con `DeferredRegister` (Forge).
- Hooks idiomáticos de Forge: `IForgeItem#mineBlock` para bonus de drops y `getDestroySpeed`/tier para velocidad, en lugar de procedimientos tipo MCreator.
- `RandomSource` en lugar de `Math.random()`.
- Artefactos técnicos en inglés; documentación de proyecto en español neutral.
- Commits: Conventional Commits, sin atribución a IA.

## Capacidades de testing

| Capacidad | Estado |
| :--- | :--- |
| `./gradlew build` | no aplicable aún (sin proyecto Gradle) |
| `./gradlew test` (JUnit 5) | disponible tras bootstrap — lógica pura |
| GameTest de Forge | disponible tras bootstrap — worldgen y drops |
| Verificación in-game | `./gradlew runClient` / `runServer` |

**Strict TDD: activado para lógica pura** (tabla de probabilidades, tier → durabilidad,
tier → recurso, carga de config). Runner: `./gradlew test`.

## Restricciones no negociables

1. Nunca copiar código, assets, nombres ni estructura de clases del original.
2. Nunca versionar el JAR original, código descompilado ni capturas del mismo.
3. `recursos/` permanece ignorado por git y se usa solo como referencia.
4. Ningún build roto pasa a la siguiente fase.
5. No crear ni integrar assets sin aprobación explícita del usuario.
6. No publicar en CurseForge, Modrinth ni GitHub sin autorización explícita.
7. No inventar funcionalidad que no exista en el original sin consultar antes.

## Decisiones abiertas

- **Package root**: `com.rexthar.desertminingreborn` — definido, la Fase A puede arrancar.
- **Assets**: el usuario los proveerá; confirmar que son texturas propias antes de la Fase G.
- **Migración futura a 1.21+/NeoForge**: fuera de alcance por ahora.
