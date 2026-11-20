
# AGENTS.md

## 1. Descripción del Proyecto

**Nombre:** Desert Mining Reborn
**Objetivo:** Portar el mod **Desert Mining** (actualmente solo para **Forge 1.20.1**) a la versión más reciente de Minecraft (objetivo: **1.21.11** o **26.1**), utilizando un enfoque de **clean room**: se analiza la funcionalidad del mod original para reescribirla desde cero con las APIs modernas del loader elegido.

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
| **Minecraft objetivo** | **1.21.11** (estable) o **26.1** (si el ecosistema de mods ya lo soporta) |
| **Loader principal** | **NeoForge** (sucesor natural de Forge para 1.21+) |
| **Loader secundario (opcional)** | Fabric (si se decide multi-loader más adelante) |
| **Java** | **21** para 1.21.11; **25** para 26.1+ |
| **Gradle** | Última versión compatible con el proyecto generado |
| **IDE** | IntelliJ IDEA 2025.2+ (soporte Java 25 para 26.1) |
| **Sistema de build** | NeoForge **ModDevGradle** (o el generador oficial de NeoForge para 1.21.11) |

### Reglas del stack
- **NO usar MCreator** en ninguna fase.
- **NO usar mappings de Yarn** en NeoForge; 1.21+ usa mappings oficiales de Mojang.
- **NO usar** `modImplementation` / `modApi` si se elige Fabric (reemplazados por `implementation` / `api`).
- **NO usar** `remapJar` en Fabric 26.x (reemplazado por `jar`).

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
- El `.jar` original (versión 1.20.1) debe permanecer en `/reference/` (ignorada por git).

---

## 4. Reglas Legales y Éticas (NO NEGOCIABLES)

1. **NUNCA** copiar código del mod original al proyecto, ni siquiera parcialmente.
2. **NUNCA** extraer texturas, modelos, sonidos o archivos de datos del JAR original para usarlos en el mod.
3. **NUNCA** subir a Git: JAR original, código descompilado, capturas del código descompilado.
4. Todo asset (texturas, modelos, sonidos, nombres) debe crearse desde cero.
5. Los nombres de bloques, items y clases deben ser **propios** (ej. `desert_mining_reborn:` en lugar de `desert_mining:`).
6. Se permite consultar la **funcionalidad** (qué hace el mod), pero la **implementación** debe ser original.
7. La carpeta `/reference/` debe estar en `.gitignore`.

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
1. **Bloques de mena de arena** en el desierto (mínimo 3–5 variantes).
2. **Objetos "fragmento de arena"** obtenidos al romper las menas.
3. **Tamices** con diferentes niveles y durabilidad (desde madera/piedra hasta netherite).
4. **Tres palas** con modificadores de velocidad y probabilidad de drop.
5. **Sistema de procesamiento**: usar el tamiz sobre los fragmentos para obtener recursos vanilla o propios.
6. **Configuración** (`config/desert-mining-reborn.toml`): activar/desactivar menas, ajustar probabilidades, durabilidad de tamices, etc.

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
1. Colocar el JAR de Desert Mining 1.20.1 en `/reference/original.jar`.
2. Usar REA: `inspect_java_archive` para obtener la estructura general.
3. Usar REA: `decompile_java_class` sobre las clases clave (registro de bloques, items, mecánicas de tamiz y palas).
4. **Documentar la funcionalidad** en `/reference/notes.md` (descripción en lenguaje natural, no copiar código).
5. Confirmar que `/reference/` está en `.gitignore`.

### Fase 1 — Bootstrap del proyecto
1. Generar un proyecto nuevo con el **generador de NeoForge** para **1.21.11** (o 26.1 si está disponible).
2. Verificar:
   - Java 21 configurado (o Java 25 si se apunta a 26.1).
   - `./gradlew build` compila el template vacío sin errores.
   - ModDevGradle configurado correctamente.
3. Configurar `.gitignore`:
   ```
   /reference/
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
1. Añadir las menas de arena al **worldgen** del desierto:
   - NeoForge: `BiomeModifier` o JSON worldgen (`ConfiguredFeature`, `PlacedFeature`).
2. Configurar la altura y frecuencia de spawn.
3. Verificar en mundo de prueba con `/locate` o generando chunks.

### Fase 4 — Lógica de tamizado y palas
1. Implementar la lógica del **tamiz**: al usarlo sobre un "fragmento de arena", consumir durabilidad y devolver un recurso (con una tabla de probabilidades).
2. Implementar los efectos de las **palas**:
   - La pala de la suerte debe modificar el `LootContext` para aumentar la probabilidad de drops.
   - La pala rápida debe modificar la velocidad de minado.
   - La pala maestra debe combinar ambos efectos.
3. Usar **Data Components** (1.20.5+) para almacenar durabilidad o estados personalizados, si es necesario.

### Fase 5 — Loot tables y recetas
1. Crear las **loot tables JSON** para las menas en `data/desert-mining-reborn/loot_tables/blocks/`.
2. Crear las **recetas** para los tamices y las palas.
3. Probar drops y crafteos en el juego.

### Fase 6 — Configuración
1. Implementar `ModConfig` con `ModConfigSpec` (NeoForge).
2. Cargar los valores desde `desert-mining-reborn.toml`.
3. Aplicar los valores en el worldgen, las loot tables y la lógica de los tamices/palas.

### Fase 7 — Assets originales
1. Crear texturas propias 16x16 para cada mena, fragmento, tamiz y pala (Aseprite, GIMP o Krita).
2. Crear `blockstates`, `models` y `lang/en_us.json` / `lang/es_es.json` con nombres propios.
3. **NO** usar texturas del mod original como base ni como referencia directa.

### Fase 8 — Testing y pulido
1. Compilar: `./gradlew build`.
2. Probar en cliente y servidor.
3. Verificar que el `.jar` final no contenga restos de `/reference/`.

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
- Seguir las convenciones de Java 21/25 (records, sealed classes, pattern matching).
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
├── worldgen/
│   ├── ModConfiguredFeatures.java
│   ├── ModPlacedFeatures.java
│   └── ModBiomeModifiers.java
├── logic/
│   ├── SieveLogic.java
│   └── ShovelEffects.java
├── config/
│   └── DesertMiningRebornConfig.java
└── util/
    └── ModConstants.java

src/main/resources/
├── assets/desert-mining-reborn/
│   ├── blockstates/
│   ├── models/
│   ├── textures/
│   └── lang/
├── data/desert-mining-reborn/
│   ├── loot_tables/
│   ├── recipes/
│   └── worldgen/
└── META-INF/neoforge.mods.toml
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
| Guía de porteo Fabric 1.21.11 | https://docs.fabricmc.net/1.21.11/develop/porting/ |
| NeoForge Migration Primer 1.21.11 → 26.1 | https://docs.neoforged.net/primer/docs/26.1/ |
| NeoForge para 26.1 (snapshots) | https://neoforged.net/ |
| Vineflower | https://github.com/Vineflower/vineflower |
| REA | https://github.com/morluto/rea |
| OpenCode Docs | https://opencode.ai/docs |
| Mod original (solo referencia conceptual) | https://www.curseforge.com/minecraft/mc-mods/desert-mining |

---

## 10. Advertencias para el Agente

- ⚠️ **No inventar funcionalidad** que no exista en el mod original sin consultar antes al usuario.
- ⚠️ **No copiar la estructura de clases** del código descompilado al pie de la letra; reescribir con patrones modernos (`DeferredRegister`/`Registry`, `MapCodec`, etc.).
- ⚠️ **No omitir la compilación** después de cada fase. Un build roto debe resolverse antes de avanzar.
- ⚠️ **No crear assets** sin antes confirmar que el usuario aprobó la lista de menas, tamices y palas.
- ⚠️ **No publicar** en ningún registro público (CurseForge, Modrinth, GitHub) sin autorización explícita del usuario.
- ⚠️ **No eliminar** `/reference/` ni su contenido sin autorización.
- ⚠️ **Verificar** en cada release que el `.jar` no incluya nada de `/reference/`.
- ⚠️ **Tener en cuenta los cambios de 1.20.5+**: el sistema de **Data Components** reemplazó al NBT para datos de items. Las loot tables y recetas también cambiaron su estructura en versiones recientes.

---

## 11. Historial de Cambios del Proyecto

| Fecha | Fase | Notas |
| :--- | :--- | :--- |
| [FECHA] | Fase 0 | Inicio del proyecto, análisis del JAR de Desert Mining 1.20.1 |
| | | |
