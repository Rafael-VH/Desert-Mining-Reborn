# 03 — Recetas

Todas las recetas verificadas contra los JSON del original.
Convención de marcas: ver [`README.md`](README.md).

Los IDs de esta tabla son los **del original** (`desert_mining:`). En nuestro mod pasan a
`desert_mining_reborn:`.

## Índice

1. [Tamices](#1-tamices)
2. [Recetas de tamizado](#2-recetas-de-tamizado)
3. [Mejoras de pala](#3-mejoras-de-pala)
4. [Palas](#4-palas)
5. [Resumen de ingredientes](#5-resumen-de-ingredientes)

---

## 1. Tamices

### 1.1 Tamiz de arena — `sandy_hand_sieve` **[R]**

Crafteo con forma. Patrón 3×3:

```
A B A
B · B
A B A
```

- **A** = `#minecraft:sandstone` (cualquier arenisca)
- **B** = `minecraft:stick`
- **·** = vacío

**Durabilidad resultante**: 4.
**Coste**: 4 areniscas + 4 palos.

### 1.2 Tamiz reforzado — `reinforced_hand_sieve` **[R]**

Mismo patrón, materiales distintos:

```
A B A
B · B
A B A
```

- **A** = `minecraft:iron_ingot`
- **B** = `minecraft:string`

**Durabilidad resultante**: 32.
**Coste**: 4 lingotes de hierro + 4 hilos.

### 1.3 Tamiz avanzado — `advanced_hand_sieve` **[R]**

Mismo patrón otra vez:

```
A B A
B · B
A B A
```

- **A** = `minecraft:gold_ingot`
- **B** = `minecraft:string`

**Durabilidad resultante**: 128.
**Coste**: 4 lingotes de oro + 4 hilos.

> **Importante**: los tres primeros tamices **no dependen entre sí**. No hay que fabricar el de
> arena para hacer el reforzado. Son recetas paralelas con materiales distintos. Esto es un desvío
> respecto a la intuición de "escalera de upgrades".

### 1.4 Tamiz con punta de diamante — `diamond_tipped_hand_sieve` **[R]**

**Mesa de herrería** (`smithing_transform`), sin plantilla:

| Ranura | Item |
| :--- | :--- |
| Plantilla | *(vacía)* |
| Base | `desert_mining:advanced_hand_sieve` |
| Adición | `minecraft:diamond` |

**Durabilidad resultante**: 512.
**Coste**: 1 tamiz avanzado (128 usos restantes o los que queden) + 1 diamante.

### 1.5 Tamiz de netherita — `enhanced_hand_sieve` **[R]**

**Mesa de herrería**:

| Ranura | Item |
| :--- | :--- |
| Plantilla | *(vacía)* |
| Base | `desert_mining:diamond_tipped_hand_sieve` |
| Adición | `minecraft:netherite_ingot` |

**Durabilidad resultante**: 2048.
**Coste**: 1 tamiz con punta de diamante + 1 lingote de netherita.

---

## 2. Recetas de tamizado

Cinco recetas **sin forma** (`crafting_shapeless`), una por fragmento **[R]**. Todas comparten la
misma estructura: `fragmento + #hand_sieves`.

| Ingrediente 1 | Ingrediente 2 | Resultado |
| :--- | :--- | :--- |
| `sandy_gold_chunk` | cualquiera de `#desert_mining:hand_sieves` | `minecraft:gold_ingot` ×1 |
| `sandy_redstone_chunk` | idem | `minecraft:redstone` ×1 |
| `sandy_lapis_chunk` | idem | `minecraft:lapis_lazuli` ×1 |
| `sandy_diamond_chunk` | idem | `minecraft:diamond` ×1 |
| `sandy_emerald_chunk` | idem | `minecraft:emerald` ×1 |

**Ratio**: 1 fragmento → 1 recurso. Sin pérdida ni ganancia.

El tamiz **no se consume**: se daña 1 punto y queda en la cuadrícula. Ver
[`02-interacciones.md`](02-interacciones.md) §3.2.

---

## 3. Mejoras de pala

### 3.1 Mejora afortunada — `fortunate_shovel_upgrade` **[R]**

```
A A A
B C B
A A A
```

- **A** = `minecraft:diamond`
- **B** = `minecraft:diamond_block`
- **C** = `minecraft:sand`

**Coste**: 7 diamantes + 2 bloques de diamante + 1 arena. Es caro.
**Resultado**: 1 item, stack de 64.

### 3.2 Mejora veloz — `speedy_shovel_upgrade` **[R]**

```
A A A
B C B
A A A
```

- **A** = `minecraft:gold_ingot`
- **B** = `minecraft:gold_block`
- **C** = `minecraft:sand`

**Coste**: 7 lingotes de oro + 2 bloques de oro + 1 arena.

### 3.3 Mejora maestra — `master_shovel_upgrade` **[R]**

```
A B A
C D E
A B A
```

- **A** = `minecraft:diamond`
- **B** = `minecraft:gold_ingot`
- **C** = `desert_mining:fortunate_shovel_upgrade`
- **D** = `minecraft:netherite_block`
- **E** = `desert_mining:speedy_shovel_upgrade`

**Coste**: 4 diamantes + 4 lingotes de oro + 1 bloque de netherita + las dos mejoras anteriores.

**Este es el cuello de botella de la progresión**: exige haber fabricado antes la mejora afortunada
y la veloz, más un bloque de netherita completo (9 lingotes).

---

## 4. Palas

Las tres comparten el mismo patrón **[R]**: **mesa de herrería** con pala de netherita como base.

| Resultado | Plantilla | Base | Adición |
| :--- | :--- | :--- | :--- |
| `fortunate_shovel` | *(vacía)* | `minecraft:netherite_shovel` | `fortunate_shovel_upgrade` |
| `speedy_shovel` | *(vacía)* | `minecraft:netherite_shovel` | `speedy_shovel_upgrade` |
| `master_shovel` | *(vacía)* | `minecraft:netherite_shovel` | `master_shovel_upgrade` |

**La pala de netherita se consume.** Es una mejora, no un crafteo paralelo.

### Nota técnica: `template: []` **[V]**

El original usa `"template": []` en `smithing_transform`. En 1.20.1 la mesa de herrería tiene tres
ranuras; una plantilla vacía obliga a dejar la ranura vacía. Funciona porque el mod se distribuye
así, pero hay que **verificarlo explícitamente al implementar**.

**Plan B [P]** si falla: receta `crafting_shaped` con la pala de netherita en el centro, o una
receta personalizada con `RecipeSerializer` propio.

---

## 5. Resumen de ingredientes

| Material vanilla | Usos en el mod |
| :--- | :--- |
| Arena | mejora afortunada, mejora veloz |
| Arenisca (`#minecraft:sandstone`) | tamiz de arena |
| Palo | tamiz de arena |
| Hilo (`string`) | tamiz reforzado, tamiz avanzado |
| Lingote de hierro | tamiz reforzado |
| Lingote de oro | tamiz avanzado, mejora veloz, mejora maestra |
| Diamante | tamiz punta de diamante, mejora afortunada, mejora maestra |
| Bloque de diamante | mejora afortunada |
| Bloque de oro | mejora veloz |
| Bloque de netherita | mejora maestra |
| Lingote de netherita | tamiz de netherita |
| Pala de netherita | las tres palas |

**Observación de balance [P]**: el mod consume enormes cantidades de diamante y oro, y exige dos
bloques de netherita en total (uno para la mejora maestra, más la pala base). Es una progresión de
final de juego, coherente con el texto de CurseForge ("la pala maestra... aún mayor velocidad").

## 6. Pendiente

- Recetas exactas de reparación del tier de las palas **[V]**.
- Decidir si se añade alguna receta que el original no tiene (p. ej. bloque de mena). **No** hacerlo
  sin aprobación: sería funcionalidad inventada.
