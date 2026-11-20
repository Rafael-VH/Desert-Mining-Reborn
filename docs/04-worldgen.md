# 04 — Worldgen

Cómo se generan las menas. Convención de marcas: ver [`README.md`](README.md).

## 1. Valores exactos **[R]**

Los cinco minerales comparten **exactamente** la misma configuración; solo cambia el bloque
resultante. Verificado en los 15 JSON del original.

### 1.1 `configured_feature` — un archivo por mena

```json
{
  "type": "minecraft:ore",
  "config": {
    "size": 4,
    "discard_chance_on_air_exposure": 0,
    "targets": [
      {
        "target": {
          "predicate_type": "minecraft:blockstate_match",
          "block_state": { "Name": "minecraft:sand" }
        },
        "state": { "Name": "<namespace>:sandy_X_ore" }
      }
    ]
  }
}
```

| Parámetro | Valor | Significado |
| :--- | :--- | :--- |
| `size` | 4 | bloques por veta (antes de los reemplazos fallidos) |
| `discard_chance_on_air_exposure` | 0 | **nunca** descarta la veta por estar expuesta al aire |
| bloque objetivo | `minecraft:sand` | **solo arena**, nunca arenisca |
| bloque resultante | la mena | |

### 1.2 `placed_feature` — un archivo por mena

```json
{
  "feature": "<namespace>:sandy_X_ore",
  "placement": [
    { "type": "minecraft:count", "count": 4 },
    { "type": "minecraft:in_square" },
    {
      "type": "minecraft:height_range",
      "height": {
        "type": "minecraft:uniform",
        "min_inclusive": { "absolute": 0 },
        "max_inclusive": { "absolute": 255 }
      }
    },
    { "type": "minecraft:biome" }
  ]
}
```

| Parámetro | Valor |
| :--- | :--- |
| intentos por chunk | 4 |
| distribución XZ | `in_square` (dentro del chunk) |
| altura | **uniforme, absoluta, 0 a 255** |
| filtro | solo dentro del bioma del modificador |

### 1.3 `forge:biome_modifier` — un archivo por mena

```json
{
  "type": "forge:add_features",
  "biomes": "desert",
  "features": "<namespace>:sandy_X_ore",
  "step": "underground_ores"
}
```

| Parámetro | Valor |
| :--- | :--- |
| bioma | `desert` — **bioma único, no un tag** |
| step | `underground_ores` |

---

## 2. Densidad resultante

Cuentas rápidas por chunk de desierto:

| Concepto | Cálculo | Total |
| :--- | :--- | ---: |
| Intentos de veta | 5 menas × 4 | 20 |
| Bloques potenciales | 20 × 4 | hasta 80 |

Y como `discard_chance_on_air_exposure = 0`, **ninguna** veta se descarta por estar expuesta. Esto
explica la descripción de CurseForge: *"aparecen en el desierto y son muy fáciles de encontrar"*.

---

## 3. El problema del rango 0–255

El original genera en **toda la columna**, de bedrock a build limit, sin distinción de capa.

### 3.1 Consecuencias observables

1. **Menas en superficie**: si hay arena en la superficie del desierto (lo normal), aparecen menas
   visibles a cielo abierto. Esto es lo que hace al mod fácil, pero rompe la estética de "minería".
2. **Menas en cuevas**: cualquier bolsa de arena en una cueva se convierte parcialmente en mena,
   flotando o al borde del vacío.
3. **Interacción con la gravedad** (ver [`02-interacciones.md`](02-interacciones.md) §1.3): una mena
   expuesta al quitar el bloque de abajo **cae**. En superficie esto significa que una veta puede
   derrumbarse sobre el jugador.
4. **Arenisca intacta**: como solo se reemplaza `minecraft:sand`, la arenisca que forma la capa
   inferior del desierto queda limpia. Las vetas se concentran en los bolsones de arena.

### 3.2 Decisión pendiente (D4)

| Opción | Descripción | Pros | Contras |
| :--- | :--- | :--- | :--- |
| **A — Fiel** **[R]** | 0–255 tal cual | comportamiento idéntico | vetas flotantes, derrumbes, estética rota |
| **B — Por capas** **[P]** | Y distinto por mineral, como el vanilla | coherente con la minería | ya no es fiel al original |
| **C — Configurable** **[P]** | Rango en config, default distinto por mineral | lo mejor de ambos | más código |

**Recomendación**: **C**, con default razonable por mineral. Documentar el desvío.

Rangos sugeridos **[P]** (deben aprobarse antes de implementar):

| Mena | Y mín | Y máx |
| :--- | ---: | ---: |
| Oro | 0 | 64 |
| Redstone | 0 | 32 |
| Lapislázuli | 0 | 48 |
| Diamante | 0 | 24 |
| Esmeralda | 0 | 96 |

---

## 4. Mejoras propuestas al worldgen **[P]**

### 4.1 Bioma por tag en lugar de bioma único

El original usa `"biomes": "desert"`. Eso excluye desiertos de datapacks y otros mods.

**Propuesta**: tag de bioma propio `#desert_mining_reborn:sandy_ore_biomes` que por defecto
contiene solo `minecraft:desert`. Extensible sin tocar código.

### 4.2 Modificador en código, no en JSON

Para que la config pueda controlar el worldgen, implementar el `BiomeModifier` **en Java** con
`DeferredRegister<BiomeModifier>` y su codec, en lugar de los 5 JSON de
`data/<ns>/forge/biome_modifier/`.

Así el jugador puede apagar minerales individualmente desde `desert-mining-reborn.toml` sin editar
datapacks. Ver [`05-configuracion.md`](05-configuracion.md) §2.

Costo: un poco más de código Java. Beneficio: la config deja de ser decorativa.

### 4.3 `discard_chance_on_air_exposure`

Mantener en `0` si se quiere fidelidad. Si se restringe el rango Y, se puede subir a `0.5` **[P]**
para evitar vetas flotantes en cuevas.

---

## 5. Estructura de archivos

### 5.1 Como lo hace el original **[R]**

```
data/
├── desert_mining/
│   ├── worldgen/
│   │   ├── configured_feature/sandy_{gold,redstone,lapis,diamond,emerald}_ore.json
│   │   └── placed_feature/sandy_{gold,redstone,lapis,diamond,emerald}_ore.json
│   └── forge/biome_modifier/sandy_*_ore_biome_modifier.json
└── minecraft/tags/blocks/mineable/shovel.json
```

### 5.2 Formato 1.20.1 a respetar

| Ruta | Nota |
| :--- | :--- |
| `worldgen/configured_feature/` | plural |
| `worldgen/placed_feature/` | plural |
| `forge/biome_modifier/` | carpeta de Forge |
| `tags/blocks/` | plural |
| `tags/items/` | plural |
| `recipes/` | plural |
| `loot_tables/blocks/` | plural |

A partir de 1.21 muchas de estas carpetas se singularizaron (`recipe`, `loot_table`, `tags/item`).
**En 1.20.1 van en plural.** Un error frecuente al copiar ejemplos modernos.

---

## 6. Verificación

| Qué | Cómo |
| :--- | :--- |
| Las menas aparecen | crear mundo con bioma desierto, contar visualmente |
| Densidad | `/execute` o GameTest contando bloques por chunk |
| Altura | minar de arriba abajo y registrar Y de cada mena |
| Solo reemplaza arena | comprobar que la arenisca y la piedra están intactas |
| Derrumbe | minar el bloque bajo una mena y observar la caída |

**Sin proyecto Gradle no se puede verificar nada de esto.** La Fase A es prerequisito.
