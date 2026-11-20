# 01 — Contenido del mod

Catálogo completo de bloques y items con sus valores exactos.
Convención de marcas: ver [`README.md`](README.md).

## Resumen

| Categoría | Cantidad | IDs |
| :--- | ---: | :--- |
| Menas | 5 | `sandy_{gold,redstone,lapis,diamond,emerald}_ore` |
| Fragmentos | 5 | `sandy_{gold,redstone,lapis,diamond,emerald}_chunk` |
| Tamices | 5 | `sandy`, `reinforced`, `advanced`, `diamond_tipped`, `enhanced` `_hand_sieve` |
| Palas | 3 | `fortunate_shovel`, `speedy_shovel`, `master_shovel` |
| Mejoras de pala | 3 | `{fortunate,speedy,master}_shovel_upgrade` |
| **Total** | **21** | todas bajo el namespace propio |

El namespace del original es `desert_mining:`. El nuestro será **`desert_mining_reborn:`** — nunca
reusar el namespace ajeno.

---

## 1. Menas (5 bloques)

### 1.1 Propiedades comunes **[R]**

Verificado en las cinco clases del original: todas idénticas salvo el estado que producen.

| Propiedad | Valor | Notas |
| :--- | :--- | :--- |
| Clase base | `FallingBlock` | **Las menas caen como arena** |
| Instrumento de nota | `NoteBlockInstrument.SNARE` | sonido al golpear con nota |
| Sonido | `SoundType.SAND` | |
| Dureza / resistencia | `4.0` ambas (`strength(4.0F)`) | arena vanilla es 0.5 |
| Emisión de luz | `15` **[V]** | ver nota abajo |
| Requiere herramienta para drop | sí (`requiresCorrectToolForDrops`) | |
| Tag de minado | `minecraft:mineable/shovel` | reemplaza el tag vanilla |
| Nivel mínimo de pala | `getTier().getLevel() >= 2` (hierro) | **[R]** ver §1.3 |
| Tooltip | ninguno | |

**[V] Emisión de luz 15**: el original sobrescribe el método ofuscado `m_7753_` devolviendo 15, con
firma compatible con `getLightEmission(BlockState, BlockGetter, BlockPos)`. El mapeo exacto debe
confirmarse al implementar; si se confirma, la mena ilumina el desierto como una antorcha.

### 1.2 Tabla de menas

| ID (namespace propio) | Mena de | Estado que reemplaza | Drop |
| :--- | :--- | :--- | :--- |
| `sandy_gold_ore` | oro | `minecraft:sand` | `sandy_gold_chunk` |
| `sandy_redstone_ore` | redstone | `minecraft:sand` | `sandy_redstone_chunk` |
| `sandy_lapis_ore` | lapislázuli | `minecraft:sand` | `sandy_lapis_chunk` |
| `sandy_diamond_ore` | diamante | `minecraft:sand` | `sandy_diamond_chunk` |
| `sandy_emerald_ore` | esmeralda | `minecraft:sand` | `sandy_emerald_chunk` |

### 1.3 Selección de herramienta **[P] — desvío consciente**

El original sobrescribe `canHarvestBlock` con un `instanceof ShovelItem` y compara
`getTier().getLevel() >= 2` **[R]**. Eso tiene dos problemas:

1. **Rompe datapacks**: un datapack que añada una pala a `minecraft:mineable/shovel` no funciona,
   porque la comprobación es por clase Java, no por tag.
2. **Ignora herramientas modificadas** de otros mods que no extiendan `ShovelItem`.

**Propuesta [P]**: eliminar el override y usar el sistema estándar —
`requiresCorrectToolForDrops()` + el block tag `minecraft:mineable/shovel` con el nivel de pala
declarado en el tag. Resultado funcional equivalente para vanilla, pero extensible.

Si se prefiere fidelidad estricta al original, restaurar el override y documentar la limitación.

### 1.4 Nombres propuestos **[P]**

| ID | Nombre ES | Nombre EN |
| :--- | :--- | :--- |
| `sandy_gold_ore` | Mena de oro arenosa | Sandy Gold Ore |
| `sandy_redstone_ore` | Mena de redstone arenosa | Sandy Redstone Ore |
| `sandy_lapis_ore` | Mena de lapislázuli arenosa | Sandy Lapis Ore |
| `sandy_diamond_ore` | Mena de diamante arenosa | Sandy Diamond Ore |
| `sandy_emerald_ore` | Mena de esmeralda arenosa | Sandy Emerald Ore |

---

## 2. Fragmentos (5 items)

| Propiedad | Valor |
| :--- | :--- |
| Clase | `Item` simple |
| Stack | `64` **[R]** |
| Rareza | `COMMON` **[R]** |
| Tooltip | ninguno **[R]** |
| Uso | ingrediente de la receta de tamizado |

| ID | Nombre ES | Nombre EN |
| :--- | :--- | :--- |
| `sandy_gold_chunk` | Fragmento de oro arenoso | Sandy Gold Chunk |
| `sandy_redstone_chunk` | Fragmento de redstone arenoso | Sandy Redstone Chunk |
| `sandy_lapis_chunk` | Fragmento de lapislázuli arenoso | Sandy Lapis Chunk |
| `sandy_diamond_chunk` | Fragmento de diamante arenoso | Sandy Diamond Chunk |
| `sandy_emerald_chunk` | Fragmento de esmeralda arenoso | Sandy Emerald Chunk |

---

## 3. Tamices manuales (5 items)

### 3.1 Propiedades comunes **[R]**

| Propiedad | Valor |
| :--- | :--- |
| Clase | `Item` simple con durabilidad |
| Stack | `1` (herramienta) |
| Rareza | `COMMON` |
| Reparable | **no** (`isRepairable` → `false`) |
| Uso sobre bloques | **ninguno** — es un ingrediente de crafteo |

El "uso" del tamiz está en la **cuadrícula de fabricación**, no en el mundo. Ver
[`02-interacciones.md`](02-interacciones.md) §3.

### 3.2 Escalera de durabilidad **[R]**

| Tier | Durabilidad | Nombre ES | Nombre EN |
| :--- | ---: | :--- | :--- |
| 1 | 4 | Tamiz manual de arena | Sandy Hand Sieve |
| 2 | 32 | Tamiz manual reforzado | Reinforced Hand Sieve |
| 3 | 128 | Tamiz manual avanzado | Advanced Hand Sieve |
| 4 | 512 | Tamiz manual con punta de diamante | Diamond-Tipped Hand Sieve |
| 5 | 2048 | Tamiz manual de netherita | Enhanced Hand Sieve |

**Atención**: los tiers 1–3 se craftean desde cero con materiales distintos; **no** son upgrades
sucesivos del anterior. Solo los tiers 4 y 5 se obtienen forjando el anterior. Ver
[`03-recetas.md`](03-recetas.md).

### 3.3 Mecánica de desgaste **[R]**

`getCraftingRemainingItem` devuelve el mismo tamiz con `+1` de daño. Si ese daño alcanza el máximo,
devuelve `ItemStack.EMPTY` — el tamiz se destruye en la cuadrícula.

Consecuencia práctica: un tamiz de arena rinde **4 tamizadas**. Uno de netherita, **2048**.

---

## 4. Palas (3 items)

### 4.1 Tiers **[R]**

Todas declaran un `Tier` anónimo propio (no reusan `Tiers.NETHERITE`).

| Pala | Durabilidad | Velocidad | Daño extra | Nivel | Encantabilidad |
| :--- | ---: | ---: | ---: | ---: | ---: |
| Fortunate | 2031 | 9.0 | 4.0 | 3 | 2 |
| Speedy | 2031 | 12.0 | 4.0 | 3 | 2 |
| Master | 4062 | 15.0 | 4.0 | 3 | 2 |

Referencia: pala de netherita vanilla = 2031 durabilidad, velocidad 9.0.

**Lectura de la tabla**:
- **Fortunate** = durabilidad y velocidad de netherita, más el bono de drops.
- **Speedy** = durabilidad de netherita, velocidad 12.0 (+33 % sobre netherita).
- **Master** = el doble de durabilidad (4062) y velocidad 15.0 (+67 % sobre netherita).

`getLevel() = 3` es nivel **diamante**, no netherita (4). En 1.20.1 ningún bloque requiere nivel 4,
así que no hay diferencia práctica.

`getEnchantmentValue() = 2` es **muy bajo** (diamante es 10, netherita 15): estas palas son
difíciles de encantar en una mesa. Es intencional en el original **[R]**.

### 4.2 Bonus de drops **[R]**

Ambas palas con bono aplican la misma estructura: por cada mena de arena rota, tirada del 50 %, y
si sale, generan chunks extra.

| Pala | Probabilidad | Chunks extra |
| :--- | ---: | :--- |
| Fortunate | 50 % | `floor(random * (2 + fortuneLevel))` |
| Master | 50 % | `floor(random * (3 + fortuneLevel))` |
| Speedy | — | sin bono |

`fortuneLevel` es el nivel del encantamiento **Fortuna en la propia pala**, no en otro sitio.

Ejemplos con Fortuna III (`fortuneLevel = 3`):

| Pala | Rango de chunks extra |
| :--- | :--- |
| Fortunate | 0 a 4 |
| Master | 0 a 5 |

### 4.3 Rotura instantánea de arena **[R] — emergente, no código**

Tiempo de rotura con herramienta correcta: `dureza × 1.5 ÷ velocidad`.

Arena: dureza 0.5.

| Pala | Cálculo | Resultado |
| :--- | :--- | :--- |
| Fortunate | `0.5 × 1.5 ÷ 9.0 = 0.083 s` | 2 ticks |
| Speedy | `0.5 × 1.5 ÷ 12.0 = 0.0625 s` | 2 ticks |
| Master | `0.5 × 1.5 ÷ 15.0 = 0.05 s` | **1 tick (instantáneo)** |

**No implementar un caso especial de rotura instantánea.** Sale del tier. Cualquier hack sería un
defecto.

### 4.4 Obtención **[R]**

No se craftean directamente. Se forjan en mesa de herrería partiendo de una **`netherite_shovel`
vanilla** + el item de mejora correspondiente. Ver [`03-recetas.md`](03-recetas.md).

### 4.5 Nombres propuestos **[P]**

| ID | Nombre ES | Nombre EN |
| :--- | :--- | :--- |
| `fortunate_shovel` | Pala afortunada | Fortunate Shovel |
| `speedy_shovel` | Pala veloz | Speedy Shovel |
| `master_shovel` | Pala maestra | Master Shovel |

---

## 5. Mejoras de pala (3 items)

| Propiedad | Valor |
| :--- | :--- |
| Clase | `Item` simple **[R]** |
| Stack | `64` **[R]** |
| Rareza | `COMMON` **[R]** |
| Comportamiento | **ninguno** — solo ingrediente de forja **[R]** |

| ID | Nombre ES | Nombre EN |
| :--- | :--- | :--- |
| `fortunate_shovel_upgrade` | Mejora de pala afortunada | Fortunate Shovel Upgrade |
| `speedy_shovel_upgrade` | Mejora de pala veloz | Speedy Shovel Upgrade |
| `master_shovel_upgrade` | Mejora de pala maestra | Master Shovel Upgrade |

---

## 6. Registro **[P]**

Orden y estructura propuestos, idiomáticos de Forge 1.20.1:

```
com.rexthar.desertminingreborn/
├── DesertMiningReborn.java          @Mod, registro de DeferredRegister, config
├── registry/
│   ├── ModBlocks.java               5 bloques
│   ├── ModItems.java                5 BlockItem + 13 items
│   └── ModCreativeTabs.java         tab propia
├── block/
│   └── SandOreBlock.java            clase única parametrizada (5 instancias)
├── item/
│   ├── SandyChunkItem.java
│   ├── HandSieveItem.java           durabilidad + getCraftingRemainingItem
│   ├── ModShovelTier.java           Tier parametrizado (nombre, dur, velocidad, bono)
│   └── UpgradeItem.java
└── logic/
    ├── DropMath.java                puro, sin imports de Minecraft
    ├── SieveTier.java               puro
    └── SieveYieldTable.java         puro
```

**Diferencia clave con el original [P]**: el original tiene **una clase por cada cosa** (5 clases de
bloque, 5 de chunk, 5 de tamiz, 3 de pala, 3 de mejora = 21 clases casi idénticas) porque fue
generado con MCreator. Nosotros usamos **clases parametrizadas + registros en bucle**. Mismo
resultado, una fracción del código.
