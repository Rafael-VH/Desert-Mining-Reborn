# 05 — Configuración

> **Aviso**: el mod original **no tiene configuración** **[R]**. Todo lo de este documento es
> **propuesta nuestra [P]** y requiere aprobación antes de implementarse.

Convención de marcas: ver [`README.md`](README.md).

## 1. Principio

La config sirve para tres cosas concretas:

1. **Balance**: ajustar probabilidades y durabilidades sin recompilar.
2. **Convivencia con otros mods**: apagar minerales que colisionen con worldgen ajeno.
3. **Desactivar comportamientos polémicos**: la gravedad de las menas, por ejemplo.

Lo que **no** debe hacer: cambiar la identidad del mod. Si se puede desactivar todo, el mod no es
nada.

## 2. Archivo y formato

- Ruta: `config/desert_mining_reborn-common.toml` (Forge deriva el nombre del mod id)
- API: `ForgeConfigSpec` (Forge 1.20.1)
- Recarga: requiere reinicio (los valores de worldgen se leen al cargar el servidor)

## 3. Estructura propuesta

### 3.1 `[worldgen]`

```toml
[worldgen]
    # Apagar minerales individualmente
    enableSandyGoldOre = true
    enableSandyRedstoneOre = true
    enableSandyLapisOre = true
    enableSandyDiamondOre = true
    enableSandyEmeraldOre = true

    # Rango vertical por mineral (D4)
    goldMinY = 0
    goldMaxY = 64
    redstoneMinY = 0
    redstoneMaxY = 32
    lapisMinY = 0
    lapisMaxY = 48
    diamondMinY = 0
    diamondMaxY = 24
    emeraldMinY = 0
    emeraldMaxY = 96

    # Intentos por chunk (el original usa 4 para todos)
    veinsPerChunk = 4

    # Tamaño de veta (el original usa 4 para todos)
    veinSize = 4

    # Descarte por exposición al aire (el original usa 0)
    discardChanceOnAirExposure = 0.0

    # Gravedad de las menas (el original las hace FallingBlock)
    fallingOres = true
```

### 3.2 `[sieve]`

```toml
[sieve]
    # Durabilidad por tier
    sandyUses = 4
    reinforcedUses = 32
    advancedUses = 128
    diamondTippedUses = 512
    enhancedUses = 2048

    # Si false, el tamiz se consume en vez de desgastarse
    damageOnCraft = true
```

### 3.3 `[shovels]`

```toml
[shovels]
    # Probabilidad de disparar el bono (original: 0.5)
    bonusChance = 0.5

    # Constante base de la fórmula floor(random * (base + fortuneLevel))
    fortunateBase = 2
    masterBase = 3

    # Velocidades de tier (original: 9.0 / 12.0 / 15.0)
    fortunateSpeed = 9.0
    speedySpeed = 12.0
    masterSpeed = 15.0

    # Durabilidades (original: 2031 / 2031 / 4062)
    fortunateDurability = 2031
    speedyDurability = 2031
    masterDurability = 4062
```

### 3.4 `[processing]`

```toml
[processing]
    # Cuántos recursos da tamizar un fragmento (original: 1)
    outputPerChunk = 1

    # Tabla de probabilidades si se quiere algo más complejo que 1:1
    # (vacío en la propuesta inicial: el original es 1:1 determinista)
```

## 4. Advertencia: config y worldgen en JSON

**Este es el punto técnico delicado.**

En Forge 1.20.1, los `configured_feature` y `placed_feature` son JSON de datapack. **Un JSON no
puede leer la config TOML.** Por lo tanto:

| Enfoque | ¿La config funciona? |
| :--- | :--- |
| JSON de worldgen + `forge:biome_modifier` JSON | **no** — la config sería decorativa |
| `BiomeModifier` en Java registrado por código | **sí** — puede leer `ForgeConfigSpec` |
| Datagen que genera JSON al compilar | solo en tiempo de build, no en runtime |

**Recomendación [P]**: implementar el `BiomeModifier` en Java. Es la única forma de que
`enableSandyDiamondOre = false` haga algo real.

Consecuencia: se pueden conservar los `configured_feature` y `placed_feature` como JSON (son
estáticos), y reemplazar solo los 5 `forge:biome_modifier` JSON por un único modificador Java que
consulta la config.

## 5. Lo que NO va en config **[P]**

- Recetas: no se pueden desactivar por TOML sin eliminar recetas en runtime, y eso rompe el
  recipe book. Fuera.
- Modelos y texturas: siempre presentes.
- La existencia de los items: el mod siempre registra los 21 elementos. La config controla
  comportamiento, no registro.

## 6. Tests

| Test | Qué verifica |
| :--- | :--- |
| Defaults | los valores por defecto coinciden con los del original |
| Lectura | `ForgeConfigSpec` carga un TOML con valores alterados |
| Rangos | valores fuera de rango se rechazan o se clampean |
| Worldgen gateado | con `enableSandyDiamondOre = false` no se añade el feature |

Los tres primeros son JUnit puro. El cuarto requiere GameTest o verificación in-game.
