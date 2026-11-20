# 06 — Plan de implementación

Plan detallado para construir el mod. Las decisiones abiertas y los requisitos exactos están en
`docs/01` – `docs/05`. Convención de marcas: ver [`README.md`](README.md).

---

## 1. Punto de partida

| Hecho | Estado |
| :--- | :--- |
| Repo | un único commit, sin código |
| Proyecto Gradle | **no existe** |
| `src/` | **no existe** |
| `recursos/` | completo: JAR, extracción, Vineflower, CFR, instalador de Forge |
| Documentación funcional | completa |
| Package root | `com.rexthar.desertminingreborn` |

**No se puede escribir una sola línea de Java hasta la Fase A.** El resto del plan depende de que
Gradle compile.

---

## 2. Decisiones que bloquean

| ID | Decisión | Bloquea | Default propuesto |
| :--- | :--- | :--- | :--- |
| **D1** | ~~Package root~~ **RESUELTO**: `com.rexthar.desertminingreborn` | — | decidido |
| **D2** | Assets: origen y aprobación | Fase G | los provee el usuario |
| **D3** | ¿Menas con gravedad (`FallingBlock`)? | Fase B | sí, con toggle de config |
| **D4** | Rango Y del worldgen (fiel 0–255 vs por capas) | Fase D | por capas, configurable |

---

## 3. Estrategia

**Ocho fases con gate de build.** Ninguna fase avanza si `./gradlew build` falla.

```
A Bootstrap ──▶ B Registro ──▶ C Lógica ──▶ D Worldgen ──▶ E Datos ──▶ F Config ──▶ G Assets ──▶ H Cierre
   (gate)          (gate)         (gate)        (gate)          (gate)        (gate)        (gate)
```

**Regla de oro**: en ningún momento se abre `recursos/vineflower-desert/` o `recursos/cfr-desert/`
mientras se escribe código. Se consulta antes, se documenta en `docs/`, y se cierra. Eso es lo que
hace que el clean room sea real y no una declaración de intenciones.

---

## Fase A — Bootstrap

**Objetivo**: un proyecto Forge 1.20.1 que compila vacío.

**Estado**: **completada** (T1–T9). `./gradlew build` termina en `BUILD SUCCESSFUL` y el `.jar`
generado contiene solo `DesertMiningReborn.class`, `META-INF/mods.toml` y `pack.mcmeta`. Queda
pendiente la comprobación manual en cliente (ver criterios).

### Tareas

| ID | Tarea | Archivo |
| :--- | :--- | :--- |
| T1 | Fijar **D1** y escribirlo en `gradle.properties` | `gradle.properties` |
| T2 | Declarar versión de MC, Forge y canal de mappings | `gradle.properties` |
| T3 | Configurar ForgeGradle 6 y toolchain Java 17 | `build.gradle` |
| T4 | `settings.gradle` + wrapper de Gradle | `settings.gradle`, `gradle/wrapper/` |
| T5 | Metadata del mod | `src/main/resources/META-INF/mods.toml` |
| T6 | `pack.mcmeta` con `pack_format: 15` | `src/main/resources/pack.mcmeta` |
| T7 | Completar `.gitignore` | `.gitignore` |
| T8 | Clase principal con `@Mod` y `DeferredRegister` vacíos | `DesertMiningReborn.java` |
| T9 | **Gate**: `./gradlew build` verde | — |

### Contenido esperado de `gradle.properties`

```properties
minecraft_version=1.20.1
forge_version=47.4.28
mapping_channel=official
mapping_version=1.20.1
mod_id=desert_mining_reborn
mod_version=0.1.0
mod_group_id=com.rexthar.desertminingreborn
mod_name=Desert Mining Reborn
mod_license=MIT
```

### Contenido esperado de `.gitignore` (adiciones)

```
/run/
/build/
.gradle/
*.jar
!gradle/wrapper/gradle-wrapper.jar
```

### Criterio de aceptación

- [x] `./gradlew build` termina en `BUILD SUCCESSFUL`.
- [ ] El `.jar` generado carga en un cliente 1.20.1 con Forge 47.4.28 sin errores en el log.
- [x] No hay ningún archivo de `recursos/` en el árbol de `src/`.

---

## Fase B — Registro de contenido

**Objetivo**: los 21 elementos existen y aparecen en el inventario creativo, sin lógica.

### Tareas

| ID | Tarea | Archivo |
| :--- | :--- | :--- |
| T10 | `SandyOreBlock` parametrizado (5 instancias) | `block/SandyOreBlock.java` |
| T11 | Registrar los 5 bloques con `DeferredRegister<Block>` | `registry/ModBlocks.java` |
| T12 | Registrar los 5 `BlockItem` | `registry/ModItems.java` |
| T13 | `SandyChunkItem` parametrizado (5 instancias) | `item/SandyChunkItem.java` |
| T14 | `HandSieveItem` con durabilidad y `getCraftingRemainingItem` | `item/HandSieveItem.java` |
| T15 | `ModShovelTier` como `Tier` parametrizado | `item/ModShovelTier.java` |
| T16 | Las 3 palas | `item/ModShovels.java` |
| T17 | `UpgradeItem` (3 instancias, stack 64) | `item/UpgradeItem.java` |
| T18 | Creative tab propia | `registry/ModCreativeTabs.java` |
| T19 | **Gate**: build verde + todo visible in-game | — |

### Decisiones de implementación

- **Una clase parametrizada, no cinco.** El original tiene 21 clases casi idénticas (MCreator).
  Nosotros usamos un constructor con parámetros y registramos en bucle. Ver
  [`01-contenido.md`](01-contenido.md) §6.
- **D3 (gravedad)**: si se conserva `FallingBlock`, `SandyOreBlock extends FallingBlock`; si no,
  `extends Block`. Debe decidirse antes de T10.
- **Durabilidades de tamiz**: de `05-configuracion.md`, no hardcodeadas.

### Criterio de aceptación

- [ ] Los 5 bloques se colocan y rompen.
- [ ] Los 5 fragmentos apilan a 64.
- [ ] Los 5 tamices muestran barra de durabilidad con el máximo correcto (4/32/128/512/2048).
- [ ] Las 3 palas muestran durabilidad 2031/2031/4062.
- [ ] Las 3 mejoras apilan a 64.
- [ ] Todo aparece en la creative tab propia.

---

## Fase C — Lógica (Strict TDD)

**Objetivo**: bonos de pala y desgaste de tamiz funcionando, con tests.

### Arquitectura clave

La matemática vive en clases **Java puras**, sin un solo `import net.minecraft.*`:

```
logic/
├── DropMath.java          puro — fórmulas de bonus
├── SieveTier.java         puro — enum tier → durabilidad
└── SieveRules.java        puro — validaciones de desgaste
```

Motivo: `./gradlew test` corre en segundos sin arrancar el juego. Si la matemática estuviera mezclada
con `ItemStack`, los tests necesitarían el runtime de Minecraft y nadie los correría.

### Tareas

| ID | Tarea | TDD |
| :--- | :--- | :--- |
| T20 | `DropMath.fortunateBonusCount(random, fortuneLevel)` | RED → GREEN |
| T21 | `DropMath.masterBonusCount(random, fortuneLevel)` | RED → GREEN |
| T22 | `SieveTier` con durabilidad y recurso por tier | RED → GREEN |
| T23 | Hook `mineBlock` en la pala fortunate | RED → GREEN |
| T24 | Hook `mineBlock` en la pala master | RED → GREEN |
| T25 | Verificar que la speedy no necesita código propio | test de tier |
| T26 | **Gate**: `./gradlew test` + `./gradlew build` verdes | — |

### Fórmulas exactas **[R]**

```
fortunateBonusCount(random, fortuneLevel) = floor(random * (2 + fortuneLevel))
masterBonusCount(random, fortuneLevel)    = floor(random * (3 + fortuneLevel))
```

Ambas detrás de un gate de `random < 0.5`.

### Tests mínimos

| Test | Casos |
| :--- | :--- |
| Bonus con Fortuna 0 | resultado siempre 0–1 |
| Bonus con Fortuna III | resultado 0–4 (fortunate) / 0–5 (master) |
| Determinismo | con `RandomSource` sembrado, resultado reproducible |
| Gate de probabilidad | ~50 % de disparo en 100 000 iteraciones |
| Tier → durabilidad | los 5 valores exactos |

### Criterio de aceptación

- [ ] `./gradlew test` verde.
- [ ] Minar una mena con pala fortunate da chunks extra ~50 % de las veces.
- [ ] Con pala master el rango es mayor.
- [ ] La speedy mina visiblemente más rápido, sin código de bonus.
- [ ] **No existe** ningún caso especial de "rotura instantánea".

---

## Fase D — Worldgen

**Objetivo**: las menas aparecen en el desierto.

### Tareas

| ID | Tarea | Archivo |
| :--- | :--- | :--- |
| T27 | 5 `configured_feature` | `data/<ns>/worldgen/configured_feature/*.json` |
| T28 | 5 `placed_feature` con rango Y según **D4** | `data/<ns>/worldgen/placed_feature/*.json` |
| T29 | Tag de bioma propio | `data/<ns>/tags/worldgen/biome/sandy_ore_biomes.json` |
| T30 | `BiomeModifier` en Java que lee la config | `worldgen/SandyOreBiomeModifier.java` |
| T31 | **Gate**: verificación in-game | — |

### Parámetros **[R]**

| Parámetro | Valor del original | Nuestro default |
| :--- | :--- | :--- |
| tamaño de veta | 4 | 4 |
| intentos por chunk | 4 | 4 |
| rango Y | 0–255 | según D4 |
| bloque reemplazado | `minecraft:sand` | igual |
| `discard_chance_on_air_exposure` | 0 | 0 |
| step | `underground_ores` | igual |

### Criterio de aceptación

- [ ] En un mundo nuevo, un bioma desierto contiene las 5 menas.
- [ ] Solo reemplazan arena; arenisca y piedra intactas.
- [ ] La densidad coincide con la tabla de `04-worldgen.md` §2.
- [ ] Con un mineral desactivado en config, ese mineral no aparece.

---

## Fase E — Datos

**Objetivo**: drops y crafteo completos.

### Tareas

| ID | Tarea | Archivo |
| :--- | :--- | :--- |
| T32 | 5 loot tables de mena | `data/<ns>/loot_tables/blocks/*.json` |
| T33 | 3 recetas de tamiz con forma | `data/<ns>/recipes/*_hand_sieve_recipe.json` |
| T34 | 2 recetas de forja de tamiz | `data/<ns>/recipes/*.json` |
| T35 | 3 recetas de mejora | `data/<ns>/recipes/*_upgrade_recipe.json` |
| T36 | 3 recetas de forja de pala | `data/<ns>/recipes/*_shovel_recipe.json` |
| T37 | 5 recetas de tamizado | `data/<ns>/recipes/sandy_chunk_to_*.json` |
| T38 | Tag `hand_sieves` | `data/<ns>/tags/items/hand_sieves.json` |
| T39 | Tag `mineable/shovel` | `data/minecraft/tags/blocks/mineable/shovel.json` |
| T40 | **Gate**: verificación in-game de cada receta | — |

### Detalle de loot table **[R]**

```
pool: 1 roll
├── entrada A: chunk
│   ├── condición: NO (tool tiene silk_touch nivel >= 1)
│   └── funciones: set_count 1..1, apply_bonus fortune ore_drops
└── entrada B: bloque de mena
    ├── condición: tool tiene silk_touch nivel >= 1
    └── funciones: set_count 1..1
```

### Criterio de aceptación

- [ ] Pala de hierro → 1 chunk. Pala de piedra → nada.
- [ ] Fortuna III → más de 1 chunk con frecuencia.
- [ ] Toque de seda → bloque, no chunk.
- [ ] Las 13 recetas se pueden completar en el juego.
- [ ] El tamiz se desgasta exactamente 1 punto por tamizado y desaparece al agotarse.
- [ ] **`template: []` de la mesa de herrería funciona** (si no, aplicar el Plan B).

---

## Fase F — Configuración

**Objetivo**: `config/desert_mining_reborn-common.toml` real y aplicado.

### Tareas

| ID | Tarea |
| :--- | :--- |
| T41 | `ForgeConfigSpec` con las 4 secciones de `05-configuracion.md` |
| T42 | Registrar la spec en el constructor del `@Mod` |
| T43 | Consumir la config en worldgen (T30), tamices (T14) y palas (T15) |
| T44 | Tests de defaults y carga |
| T45 | **Gate**: build + test verdes, config aplicada in-game |

### Criterio de aceptación

- [ ] El TOML se genera con valores por defecto iguales a los del original.
- [ ] Cambiar `enableSandyDiamondOre = false` elimina esa mena del mundo.
- [ ] Cambiar `enhancedUses` cambia la durabilidad del tamiz de netherita.
- [ ] Cambiar `bonusChance` cambia la frecuencia del bono.

---

## Fase G — Assets

**Objetivo**: el mod se ve terminado. **Requiere aprobación previa del usuario (D2).**

### Tareas

| ID | Tarea |
| :--- | :--- |
| T46 | Confirmar lista final de nombres y aprobar assets |
| T47 | Integrar texturas 16×16 (las provee el usuario) |
| T48 | 5 blockstates + 5 modelos de bloque |
| T49 | 21 modelos de item (los bloques usan modelo propio) |
| T50 | `lang/en_us.json` |
| T51 | `lang/es_es.json` |
| T52 | **Gate**: revisión visual in-game |

### Inventario de archivos a crear

| Tipo | Cantidad | Ruta |
| :--- | ---: | :--- |
| texturas de bloque | 5 | `assets/<ns>/textures/block/` |
| texturas de item | 16 | `assets/<ns>/textures/item/` |
| blockstates | 5 | `assets/<ns>/blockstates/` |
| modelos de bloque | 5 | `assets/<ns>/models/block/` |
| modelos de item | 21 | `assets/<ns>/models/item/` |
| archivos de idioma | 2 | `assets/<ns>/lang/` |

### Criterio de aceptación

- [ ] Ningún cubo magenta y negro.
- [ ] Todos los nombres legibles en inglés y español.
- [ ] Ninguna textura derivada del JAR original.

---

## Fase H — Cierre

| ID | Tarea |
| :--- | :--- |
| T53 | `./gradlew build` final |
| T54 | Inspeccionar el `.jar`: sin rastros de `recursos/` ni del original |
| T55 | Prueba en servidor dedicado (no solo cliente) |
| T56 | `README.md` real: qué es, cómo se instala, crédito conceptual |
| T57 | `LICENSE` |
| T58 | Auditoría clean-room final |
| T59 | Actualizar este plan y `openspec/` con el estado real |

### Auditoría clean-room (T58)

Recorrer el código propio y responder:

- [ ] ¿Algún nombre de clase coincide con el original? (`SandyDiamondOreBlock` vs `SandyOreBlock` —
      debe ser propio)
- [ ] ¿Algún método tiene la misma estructura línea por línea?
- [ ] ¿Alguna textura es un derivado del original?
- [ ] ¿El `.jar` contiene archivos cuyo hash coincida con los del JAR original?
- [ ] ¿Quedó `recursos/` referenciado en algún lado?

---

## 4. Estrategia de testing

| Nivel | Qué cubre | Herramienta |
| :--- | :--- | :--- |
| Unitario puro | `DropMath`, `SieveTier`, config | JUnit 5, `./gradlew test` |
| GameTest | worldgen, drops, gravedad | Forge GameTest |
| In-game | recetas, crafteo, balance | manual |

**Strict TDD obligatorio** en Fase C: se escribe el test, se ve fallar, se implementa, se ve pasar.

**No** se hace TDD en assets, worldgen visual ni UI.

---

## 5. Cortes de PR

El cambio completo excede ampliamente el presupuesto de revisión de 800 líneas
(estimado 1.800–2.600). Cortes:

| PR | Contenido | Fases |
| :--- | :--- | :--- |
| #1 | Bootstrap y registro | A + B |
| #2 | Lógica con tests | C |
| #3 | Worldgen y datos | D + E |
| #4 | Configuración | F |
| #5 | Assets y cierre | G + H |

Cada PR debe compilar y dejar el mod jugable al nivel de su fase.

---

## 6. Riesgos

| Riesgo | Prob. | Impacto | Mitigación |
| :--- | :--- | :--- | :--- |
| Contaminación clean-room | alta | legal | no abrir `vineflower-desert/` mientras se codifica; auditoría T58 |
| `template: []` no funciona en 1.20.1 | media | bloquea 5 recetas | verificar en T36; Plan B con `crafting_shaped` |
| Unit tests que requieren runtime de MC | media | tests inútiles | aislar matemática en `logic/` |
| Conflicto de IDs con otros mods | baja | crash | namespace propio, `desert_mining_reborn` |
| Worldgen 0–255 | alta | jugabilidad | decisión D4 antes de T28 |
| Gravedad de las menas | media | bugs raros | decisión D3 antes de T10; toggle de config |
| Desincronización config ↔ worldgen JSON | alta | config decorativa | `BiomeModifier` en Java (T30) |

---

## 7. Definición de terminado

El proyecto está terminado cuando:

1. `./gradlew build` y `./gradlew test` pasan en un clon limpio.
2. Los 21 elementos existen, se ven bien y funcionan.
3. Las 13 recetas funcionan.
4. Las menas aparecen solo en desierto, según el rango configurado.
5. La doble bonificación por Fortuna está documentada en el tooltip.
6. El `.jar` final no contiene ningún rastro del original.
7. `docs/` refleja el estado real, no el deseado.
8. `openspec/` está sincronizado.
9. `README.md` explica qué es el mod y acredita el concepto original (sin usar su nombre ni sus
   assets).

---

## 8. Lo que este plan NO hace

- No porta el mod a 1.21 ni a NeoForge.
- No añade compatibilidad con Fabric.
- No añade funcionalidades que el original no tenga (salvo config, marcada **[P]**).
- No publica nada en ninguna plataforma.
- No modifica el JAR original ni lo redistribuye.
