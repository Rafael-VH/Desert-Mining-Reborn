# 02 — Interacciones

Cómo se comporta cada elemento añadido frente al juego base y frente al resto del mod.
Convención de marcas: ver [`README.md`](README.md).

## 0. Matriz general

| Elemento añadido | Interactúa con | Tipo de interacción |
| :--- | :--- | :--- |
| Mena | Pala vanilla (hierro+) | única herramienta válida |
| Mena | Fortuna | multiplica los fragmentos |
| Mena | Toque de seda | devuelve el bloque |
| Mena | Gravedad | cae como arena |
| Mena | Explosiones, pistones | como bloque normal (resistencia 4.0) |
| Fragmento | Tamiz (receta) | se consume |
| Tamiz | Receta sin forma | se daña 1 punto por uso |
| Pala | Fortuna | doble bonificación (loot table + efecto propio) |
| Pala | Mesa de herrería | se forja desde pala de netherita |
| Mejora de pala | Mesa de herrería | se consume en la forja |
| Todo | `minecraft:mineable/shovel` | tag aditivo, `replace: false` |

**El mod no reemplaza nada de vanilla.** Solo agrega entradas a un tag existente con
`"replace": false` **[R]**. No sobrescribe loot tables, recetas ni bloques del juego base.

---

## 1. Menas (bloques)

### 1.1 Minado

- **Herramienta**: cualquier pala. Sin pala no hay drop.
- **Nivel mínimo**: hierro **[R]**. Una pala de madera o piedra rompe el bloque pero no da nada.
  > En nuestra implementación se hace vía tag + `requiresCorrectToolForDrops` **[P]**; ver
  > [`01-contenido.md`](01-contenido.md) §1.3.
- **Dureza 4.0** **[R]**: arena vanilla es 0.5. Con pala de hierro (velocidad 6.0):
  `4.0 × 1.5 ÷ 6.0 = 1.0 s` por bloque. Es un minado lento, no instantáneo.
- **Tag `minecraft:mineable/shovel`** **[R]**: presente en `"replace": false`, así que cualquier
  pala añadida por otro mod o datapack que use ese tag también funciona.

### 1.2 Fortuna

La loot table aplica `apply_bonus` con `formula: "minecraft:ore_drops"` **solo en la entrada del
fragmento**, es decir, solo cuando **no** hay Toque de seda **[R]**.

Resultado: con Fortuna III, la cantidad de fragmentos puede multiplicarse hasta 4× **[V]** (la
fórmula exacta de `ore_drops` debe confirmarse al implementar).

### 1.3 Gravedad — la interacción más delicada

Las menas extienden `FallingBlock` **[R]**. Consecuencias:

| Situación | Qué pasa |
| :--- | :--- |
| Se mina el bloque de abajo | la mena se convierte en entidad y **cae** |
| Aterriza sobre un bloque sólido | se vuelve a colocar como bloque |
| No puede colocarse donde cae | se entrega como **item** al suelo |
| Cae sobre una antorcha o flor | probablemente se entrega como item **[V]** |
| Se rompe en el aire | no se puede minar mientras cae: es una entidad |
| Pistón la empuja estática | se mueve como bloque normal |
| Explosión | se destruye; resistencia 4.0 |

**Riesgos reales de esta mecánica:**
- Una veta de superficie puede derrumbarse sobre el jugador al minar desde abajo.
- Vetas generadas en cuevas pueden caer en cascada.
- Mina únicamente con pala de hierro o mejor: con una de piedra romperás el bloque sin drop y la
  mena caerá sin darte nada.

**Decisión pendiente (D3)**: conservar `FallingBlock` por fidelidad **[R]**, o usar un bloque sólido.
Si se conserva, exponer un toggle en config **[P]**.

### 1.4 Toque de seda

Con Toque de seda, la loot table devuelve **el bloque de mena**, no el fragmento **[R]**. Ese bloque
es colocable y, siendo `FallingBlock`, vuelve a caer si no tiene apoyo.

**Caso borde [V]**: el efecto de las palas fortunate/master se dispara en `mineBlock`, que no
consulta Toque de seda. Con Toque de seda + pala fortunate un jugador podría recibir **el bloque y
fragmentos extra a la vez**. Es un defecto del original; documentarlo y decidir si se replica o se
corrige **[P]**.

### 1.5 Redstone y bloques de nota

Instrumento `SNARE` **[R]**: al golpearlas, un bloque de nota encima produce sonido de caja.
No emiten señal de redstone (la emisión de luz es visual, no lógica) **[V]**.

---

## 2. Fragmentos

- Item inerte. **Ninguna interacción** con el mundo.
- Solo existe como ingrediente de la receta de tamizado y como objeto de inventario.
- Se puede almacenar en cofres, hornear, etc. como cualquier item; sin receta de fundición.

---

## 3. Tamices

### 3.1 La mecánica central: crafteo, no uso

El tamiz **no se usa sobre ningún bloque**. No tiene `use()`. Interactúa únicamente con la **mesa de
crafteo** **[R]**.

Receta: `1 fragmento + 1 tamiz (cualquiera del tag)` → `1 recurso vanilla`.

### 3.2 Desgaste en la cuadrícula

Cuando el crafteo se completa, `getCraftingRemainingItem` devuelve **el mismo tamiz con +1 de daño**
**[R]**. Si llega al máximo, devuelve vacío y el tamiz se destruye.

Esto significa que:
- El tamiz **no se consume** al craftear, se desgasta.
- Se puede craftear repetidamente mientras quede durabilidad.
- Al craftear con **shift-click** en la mesa, cada iteración consume 1 punto.
- Un tamiz en el suelo o en un cofre mantiene su daño (NBT).

### 3.3 Reparación

`isRepairable` devuelve `false` **[R]**: **no se puede reparar** ni en yunque ni con materiales.
Cuando se rompe, se pierde. Hay que fabricar otro.

### 3.4 Encantamientos

Es un `Item` normal, así que acepta encantamientos compatibles por categoría. En la práctica
**Irrompible** y **Reparación** no aportan nada útil: el desgaste viene de crafteo, no de uso como
herramienta. No bloquea nada, simplemente no se aprovecha **[P]**.

### 3.5 Automatización

En 1.20.1 **no existe la craftera automática** (llegó en 1.21). Por lo tanto no hay forma de
automatizar el tamizado con vanilla. Solo manual **[R]**.

### 3.6 Conflicto de recetas

Las 5 recetas de tamizado comparten el mismo patrón `fragmento + tag hand_sieves`. Como cada
fragmento es un item distinto, no hay ambigüedad **[P]**.

---

## 4. Palas

### 4.1 Minado general

Son `ShovelItem` con tier propio **[R]**, así que funcionan como cualquier pala: arena, tierra,
grava, arcilla, nieve, caminos. Nivel 3 (diamante) permite minar todo lo que una pala de diamante
mina.

| Pala | Velocidad vs netherita | Efecto sobre arena |
| :--- | ---: | :--- |
| Fortunate | igual (9.0) | 2 ticks |
| Speedy | +33 % (12.0) | 2 ticks |
| Master | +67 % (15.0) | **1 tick, instantáneo** |

### 4.2 Doble bonificación por Fortuna

Aquí está la interacción que más confunde. Con una pala fortunate o master y Fortuna en la pala:

1. La **loot table** aplica `ore_drops` y multiplica los fragmentos base.
2. El **efecto de la pala** añade fragmentos extra según `floor(random × (2 o 3 + fortuneLevel))`.

Ambos son acumulativos e intencionales **[R]**. Un jugador puede recibir many más fragmentos de los
que esperaría. Documentarlo en el tooltip para que no se reporte como bug.

### 4.3 Toque de seda

Si la pala tiene Toque de seda, la loot table devuelve el bloque, pero el efecto de la pala **sigue
disparándose** (ver §1.4). Caso borde a decidir.

### 4.4 Encantamientos

`getEnchantmentValue() = 2` **[R]**: conseguir Fortuna III en una mesa de encantamientos es
carísimo. La ruta práctica es libro + yunque **[P]**. Esto es coherente con la intención del mod:
las palas son la recompensa final.

### 4.5 Mesa de herrería

Se forjan desde una **`netherite_shovel` vanilla** + la mejora correspondiente **[R]**. La pala de
netherita **se consume** en la operación. Ver [`03-recetas.md`](03-recetas.md).

### 4.6 Durabilidad y reparación

Durabilidad 2031 (fortunate/speedy) o 4062 (master) **[R]**. Se reparan como cualquier herramienta de
su tier: con el material declarado en `Tier.getRepairIngredient()`. El original usa el mismo item
para las tres **[V]** — identificar cuál durante la implementación.

---

## 5. Mejoras de pala

- Item inerte de stack 64 **[R]**. Ninguna interacción directa con el mundo.
- Se consume en la mesa de herrería junto a una pala de netherita.
- La mejora **maestra** se craftea a partir de las otras dos mejoras + bloque de netherita **[R]**,
  así que es el cuello de botella de la progresión.

---

## 6. Progresión completa del jugador

```
Desierto
  └── minar menas con pala de hierro  →  fragmentos arenosos
        └── craftear tamiz de arena (4 usos)
              └── tamizar fragmentos  →  recursos vanilla
                    └── craftear tamices mejores (hierro, oro)
                          └── forjar tamiz de diamante (512)
                                └── forjar tamiz de netherita (2048)
        └── craftear mejoras de pala
              └── forjar pala afortunada / veloz
                    └── craftear mejora maestra (necesita las dos mejoras + bloque de netherita)
                          └── forjar pala maestra
```

---

## 7. Lo que el mod NO toca

Para auditoría clean-room y para evitar sorpresas:

- **No** sobrescribe ninguna loot table de vanilla.
- **No** sobrescribe ninguna receta de vanilla.
- **No** reemplaza tags: las dos entradas de tag usan `"replace": false`.
- **No** añade encantamientos nuevos.
- **No** modifica biomas ni features de vanilla: solo añade features propias al bioma desierto.
- **No** altera el comportamiento de ninguna herramienta vanilla.

La única escritura sobre el namespace `minecraft` es el archivo
`data/minecraft/tags/blocks/mineable/shovel.json` **[R]**.
