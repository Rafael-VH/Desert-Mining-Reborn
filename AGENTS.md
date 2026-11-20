
# AGENTS.md

## 1. Descripción del Proyecto

**Nombre:** Desert Mining Reborn
**Objetivo:** Reimplementar el mod **Desert Mining** (v1.0.0, Forge 1.20.1) para **Minecraft 1.20.1 sobre Forge**, con un enfoque de **clean room**: se analiza la funcionalidad del mod original para reescribirla desde cero. La documentación funcional vive en `docs/`.

**Estado del mod original:**
- **Última versión:** 1.0.0 para Minecraft **1.20.1** (Forge). Publicada el **6 de julio de 2024**.
- **Loaders soportados históricamente:** Forge (1.16.5, 1.18.2, 1.19.2, 1.19.4, 1.20.1).
- **Código fuente público:** **No disponible**. No se ha encontrado repositorio en GitHub ni página en Modrinth.
- **Popularidad:** ~2.3 millones de descargas en CurseForge.

**Filosofía del proyecto:** Se usa el JAR original únicamente como referencia para entender la **funcionalidad** (qué hace el mod). **No se copia código, texturas, modelos, sonidos ni nombres únicos**. Todo el código y los assets del port son originales.

---

## 2. Stack Técnico

| Componente | Valor |
| :--- | :--- |
| **Minecraft objetivo** | **1.20.1** |
| **Loader** | **Forge 47.4.28** (rama LTS de 1.20.1) |
| **Java** | **17** (el que exige 1.20.1) |
| **Gradle** | **8.x**, vía ForgeGradle 6 |
| **Mappings** | **official** (Mojang) — no Yarn |
| **IDE** | IntelliJ IDEA (Community o Ultimate) |
| **Sistema de build** | **ForgeGradle 6** sobre el MDK oficial de Forge 1.20.1 |
| **Metadata del mod** | `src/main/resources/META-INF/mods.toml` |
| **Datos de items** | **NBT** — `Data Components` llega en 1.20.5, no existe aquí |

### Reglas del stack
- **NO usar MCreator** en ninguna fase.
- **NO usar mappings de Yarn**: 1.20.1 sobre Forge usa mappings oficiales de Mojang.
- **NO usar APIs de NeoForge** (`neoforge.mods.toml`, `ModDevGradle`, `PayloadRegistrar`). Son posteriores a 1.20.1 y no compilan aquí.
- **NO usar `Data Components`** ni `MapCodec`: son de 1.20.5+. En 1.20.1 se usa NBT y `Codec`/`RecordCodecBuilder`.
- **NO usar** `modImplementation` / `modApi`: son de Fabric/Loom. Forge usa `implementation` y `fg.deobf`.

---

## 3. Herramientas de Apoyo

### 3.1 REA (Reverse Engineer Anything)
Servidor MCP para ingeniería inversa asistida por IA.
- Variable de entorno: `REA_VINEFLOWER_JAR=/ruta/a/vineflower.jar`
- Setup: `npx rea-agents setup`
- Herramientas: `inspect_java_archive`, `inspect_java_class`, `decompile_java_class`

### 3.2 Vineflower
Descompilador principal. Descargar de: https://github.com/Vineflower/vineflower
Uso **exclusivamente local** para analizar el JAR original. **Nunca subir código descompilado a Git**.

### 3.3 OpenCode
Agente de IA principal. Usa este archivo `AGENTS.md` como guía de proyecto.

### 3.4 Referencia del mod original
- CurseForge: https://www.curseforge.com/minecraft/mc-mods/desert-mining
- El `.jar` original (versión 1.20.1) y todo el material de análisis viven en `recursos/` (ignorada por git).
- `recursos/vineflower-desert/` y `recursos/cfr-desert/` son **solo referencia**: no se abren mientras se escribe código.

---

## 4. Reglas Legales y Éticas (NO NEGOCIABLES)

1. **NUNCA** copiar código del mod original al proyecto, ni siquiera parcialmente.
2. **NUNCA** extraer texturas, modelos, sonidos o archivos de datos del JAR original para usarlos en el mod.
3. **NUNCA** subir a Git: JAR original, código descompilado, capturas del código descompilado.
4. Todo asset (texturas, modelos, sonidos, nombres) debe crearse desde cero.
5. Los nombres de bloques, items y clases deben ser **propios** (ej. `desert_mining_reborn:` en lugar de `desert_mining:`).
6. Se permite consultar la **funcionalidad** (qué hace el mod), pero la **implementación** debe ser original.
7. La carpeta `recursos/` debe estar en `.gitignore` y nunca debe subirse.

---

## 5. Alcance Funcional (basado en la funcionalidad conocida del original)

El mod original, según las descripciones disponibles, añade:

- **Menas de arena** que aparecen en el desierto y se minan con **pala** (no con pico).
- Al romper las menas, se obtienen **fragmentos de arena** que deben procesarse.
- **Tamices manuales (sieves)** de varios niveles (desde un tamiz de arena barato hasta uno de netherite con 2048 usos) para transformar los fragmentos en recursos valiosos.
- **Tres tipos de palas nuevas**:
  - **Pala de la Suerte:** aumenta las probabilidades de obtener mejores drops.
  - **Pala Rápida:** más veloz que la de netherite.
  - **Pala Maestra:** combina velocidad y suerte, permitiendo romper arena instantáneamente.

### 5.1 Funcionalidades a recrear (reinterpretadas)
1. **5 bloques de mena arenosa** en el desierto: oro, redstone, lapislázuli, diamante, esmeralda.
2. **5 fragmentos arenosos** obtenidos al romper las menas con pala de hierro o mejor.
3. **5 tamices manuales** con durabilidades 4 / 32 / 128 / 512 / 2048.
4. **3 palas** (Fortunate, Speedy, Master) con velocidad y bono de drops propios.
5. **Sistema de procesamiento**: el tamiz se usa en la **mesa de crafteo**, no en el mundo. Cada tamizado consume 1 punto de durabilidad y devuelve 1 recurso vanilla.
6. **Configuración** (`ForgeConfigSpec`): toggle de menas, rangos Y, durabilidades y probabilidades.

### 5.2 Hallazgos verificados del código original

Estos hechos están confirmados leyendo el descompilado. **No se deducen de la descripción de CurseForge.** Detalle completo en [`docs/`](docs/README.md).

- Las 5 menas extienden `FallingBlock`: **caen como arena**.
- Las 5 menas emiten **luz 15**.
- Propiedades de mena: `strength(4.0)`, `SoundType.SAND`, `NoteBlockInstrument.SNARE`, `requiresCorrectToolForDrops`, nivel mínimo de pala 2 (hierro).
- Tiers de pala: Fortunate 2031/9.0, Speedy 2031/12.0, Master 4062/15.0; todas `attackDamageBonus 4.0`, `getLevel() 3`, `enchantmentValue 2`.
- Bonus de drops: `floor(random × (2 + fortune))` y `floor(random × (3 + fortune))`, ambos tras un gate del 50 %.
- "Romper arena al instante" **no es código**: sale de la velocidad 15.0. No implementar hacks.
- Worldgen idéntico en los 5 minerales: count 4, size 4, Y 0–255, target `minecraft:sand`, `discard_chance_on_air_exposure 0`, bioma `desert`, step `underground_ores`.
- El mod original **no tiene configuración**. Todo `docs/05` es propuesta nuestra.

---

## 6. Flujo de Trabajo con el Agente (OpenCode)

### Commits

Cuando el usuario pida hacer un commit, **cargar y seguir la skill especializada `git-commit`**
antes de ejecutar nada. No improvisar el mensaje ni el flujo: la skill define el análisis del diff,
el staging selectivo y el formato Conventional Commits.

Reglas que no se negocian:
- Nunca añadir atribución de IA ni `Co-Authored-By`.
- Un commit por unidad lógica de trabajo.
- No commitear secretos, credenciales ni nada dentro de `recursos/`.

### Fase 0 — Análisis del mod original (solo referencia local)
1. Colocar el JAR de Desert Mining 1.20.1 en `recursos/`.
2. Usar REA: `inspect_java_archive` para obtener la estructura general.
3. Usar REA: `decompile_java_class` sobre las clases clave (registro de bloques, items, mecánicas de tamiz y palas).
4. **Documentar la funcionalidad** en `docs/` (descripción en lenguaje natural, no copiar código).
5. Confirmar que `recursos/` está en `.gitignore`.

### Fase 1 — Bootstrap del proyecto
1. Partir del **MDK oficial de Forge 1.20.1** (Forge 47.4.28) con **ForgeGradle 6**.
2. Verificar:
   - **Java 17** configurado en el toolchain.
   - `./gradlew build` compila el template vacío sin errores.
   - Las mappings oficiales resuelven (`official` / `1.20.1`).
3. Completar `.gitignore` (además de lo que ya ignora `recursos/` y las carpetas de herramientas locales):
   ```
   /run/
   /build/
   .gradle/
   *.jar
   !gradle/wrapper/gradle-wrapper.jar
   ```

### Fase 2 — Registro de bloques, items y herramientas
1. Crear la clase principal `DesertMiningReborn`.
2. Registrar las **menas de arena** (bloques) con sus `BlockItem`.
3. Registrar los **fragmentos de arena** (items).
4. Registrar los **tamices** (items con durabilidad y lógica de uso).
5. Registrar las **tres palas** (`SwordItem`/`ShovelItem` o `ToolItem` moderno) con sus atributos.
6. Añadir todo a los creative tabs correspondientes.
7. Compilar (`./gradlew build`) y verificar en un mundo de prueba.

### Fase 3 — Generación de mundo
1. Añadir las menas arenosas al **worldgen** del desierto con `ConfiguredFeature` + `PlacedFeature` (JSON).
2. Registrar el `BiomeModifier` **en Java** con `DeferredRegister<BiomeModifier>`, para que la config pueda gobernar el spawneo. Un JSON de `forge:biome_modifier` no puede leer el TOML.
3. Configurar altura y frecuencia según `docs/04-worldgen.md`.
4. Verificar en mundo de prueba con `/locate` o generando chunks.

### Fase 4 — Lógica de tamizado y palas
1. Implementar el **tamiz** como item con durabilidad: `getCraftingRemainingItem` devuelve el mismo tamiz con +1 de daño, o `ItemStack.EMPTY` si se agota. Se usa en la **mesa de crafteo**, no sobre bloques.
2. Implementar los efectos de las **palas**:
   - **Fortunate**: hook en `mineBlock` que añade `floor(random × (2 + fortune))` fragmentos extra tras un gate del 50 %.
   - **Speedy**: sin lógica propia. Su ventaja es solo el tier (velocidad 12.0).
   - **Master**: hook en `mineBlock` con `floor(random × (3 + fortune))` tras el mismo gate.
3. **No** implementar rotura instantánea: la Master rompe arena en 1 tick por su velocidad 15.0. Es emergente.
4. La matemática de los bonos vive en clases Java puras (`logic/DropMath`), sin imports de Minecraft, para poder testear con `./gradlew test`.
5. En 1.20.1 la durabilidad se maneja con `ItemStack` + NBT. **No** usar Data Components.

### Fase 5 — Loot tables y recetas
1. Crear las **loot tables JSON** de las menas en `data/desert_mining_reborn/loot_tables/blocks/`.
2. Crear las **13 recetas**: 3 de tamiz desde cero, 2 de forja de tamiz, 3 de mejora, 3 de forja de pala y 5 de tamizado.
3. Crear el tag `data/minecraft/tags/blocks/mineable/shovel.json` (aditivo, `replace: false`).
4. Crear el tag `data/desert_mining_reborn/tags/items/hand_sieves.json`.
5. Probar drops y crafteos en el juego. Verificar que `"template": []` funcione en la mesa de herrería de 1.20.1.

### Fase 6 — Configuración
1. Implementar `ForgeConfigSpec` (Forge 1.20.1) y registrarlo en el constructor del `@Mod`.
2. Forge deriva el nombre del archivo del mod id. Con `mod_id=desert_mining_reborn` el TOML resultante es `config/desert_mining_reborn-common.toml`.
3. Aplicar los valores en el `BiomeModifier` (Fase 3), la durabilidad de los tamices y los bonos de las palas.

### Fase 7 — Assets propios
1. Crear texturas propias 16x16 para cada mena, fragmento, tamiz y pala (Aseprite, GIMP o Krita).
2. Crear `blockstates`, `models` y `lang/en_us.json` / `lang/es_es.json` con nombres propios.
3. **NO** usar texturas del mod original como base ni como referencia directa.

### Fase 8 — Testing y pulido
1. Compilar: `./gradlew build`.
2. Probar en cliente y servidor.
3. Verificar que el `.jar` final no contenga restos de `recursos/`.

### Fase 9 — Publicación (opcional)
1. Añadir `LICENSE` (recomendado: MIT o LGPL-3.0).
2. Añadir `README.md` con créditos conceptuales al mod original.
3. Publicar en CurseForge / Modrinth con atribución al concepto original (sin usar sus assets ni su nombre).
4. **NO** publicar el JAR original ni el código descompilado.

---

## 7. Convenciones de Código

### Nomenclatura
- Paquete raíz: `com.rexthar.desertminingreborn` (definido).
- Clases: PascalCase.
- Métodos/variables: camelCase.
- Constantes: SCREAMING_SNAKE_CASE.
- Prefijo de registro: `desert_mining_reborn:`.

### Estilo
- Seguir las convenciones de **Java 17** (records, sealed classes, pattern matching para `instanceof` y `switch`).
- Usar `@Override` explícito.
- Documentar todas las clases públicas con Javadoc.
- No dejar código comentado sin justificación.
- No dejar TODOs sin contexto.

### Estructura de carpetas sugerida
```
src/main/java/com/rexthar/desertminingreborn/
├── DesertMiningReborn.java
├── registry/
│   ├── ModBlocks.java
│   ├── ModItems.java
│   └── ModCreativeTabs.java
├── block/
│   └── SandyOreBlock.java
├── item/
│   ├── SandyChunkItem.java
│   ├── HandSieveItem.java
│   ├── ModShovelTier.java
│   └── UpgradeItem.java
├── worldgen/
│   └── SandyOreBiomeModifier.java
├── config/
│   └── DesertMiningRebornConfig.java
└── logic/
    ├── DropMath.java
    ├── SieveTier.java
    └── SieveRules.java

src/main/resources/
├── assets/desert_mining_reborn/
│   ├── blockstates/
│   ├── models/
│   ├── textures/
│   └── lang/
├── data/desert_mining_reborn/
│   ├── loot_tables/
│   ├── recipes/
│   ├── tags/items/
│   └── worldgen/
├── data/minecraft/tags/blocks/mineable/shovel.json
└── META-INF/mods.toml
```

---

## 8. Comandos Útiles

```bash
# Compilar
./gradlew build

# Ejecutar cliente
./gradlew runClient

# Ejecutar servidor
./gradlew runServer

# Refrescar dependencias
./gradlew --refresh-dependencies

# Limpiar
./gradlew clean
```

---

## 9. Recursos de Referencia

| Recurso | URL |
| :--- | :--- |
| Forge 1.20.1 — documentación oficial | https://docs.minecraftforge.net/en/1.20.1/ |
| Forge — descargas y MDK | https://files.minecraftforge.net/ |
| ForgeGradle | https://github.com/MinecraftForge/ForgeGradle |
| Mappings oficiales (MCPConfig) | https://github.com/MinecraftForge/MCPConfig |
| Vineflower | https://github.com/Vineflower/vineflower |
| REA | https://github.com/morluto/rea |
| OpenCode Docs | https://opencode.ai/docs |
| Mod original (solo referencia conceptual) | https://www.curseforge.com/minecraft/mc-mods/desert-mining |

---

## 10. Advertencias para el Agente

- ⚠️ **No inventar funcionalidad** que no exista en el mod original sin consultar antes al usuario.
- ⚠️ **No copiar la estructura de clases** del código descompilado al pie de la letra; reescribir con patrones idiomáticos de Forge 1.20.1 (`DeferredRegister`, `Codec`/`RecordCodecBuilder`, clases parametrizadas).
- ⚠️ **No omitir la compilación** después de cada fase. Un build roto debe resolverse antes de avanzar.
- ⚠️ **No crear assets** sin antes confirmar que el usuario aprobó la lista de menas, tamices y palas.
- ⚠️ **No publicar** en ningún registro público (CurseForge, Modrinth, GitHub) sin autorización explícita del usuario.
- ⚠️ **No eliminar** `recursos/` ni su contenido sin autorización.
- ⚠️ **Verificar** en cada release que el `.jar` no incluya nada de `recursos/`.
- ⚠️ **Cuidado al copiar ejemplos de 1.20.5+ o 1.21+**: en esas versiones `Data Components` reemplazó al NBT y varias carpetas de datos pasaron a singular (`recipe`, `loot_table`, `tags/item`). En **1.20.1** se usa NBT y las carpetas van en **plural**.
- ⚠️ **No usar APIs de NeoForge**: este proyecto es Forge 1.20.1 puro.

---

## 11. Historial de Cambios del Proyecto

| Fecha | Fase | Notas |
| :--- | :--- | :--- |
| 2026-11-19 | Fase 0 | Análisis funcional del JAR de Desert Mining 1.20.1 y documentación en `docs/` |
| 2026-11-20 | Planeación | Stack fijado en Forge 1.20.1 / Java 17. Package root definido: `com.rexthar.desertminingreborn` |
