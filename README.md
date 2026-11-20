# Desert Mining Reborn

Reimplementación **clean-room** del mod **Desert Mining** para **Minecraft 1.20.1** con **Forge**.

> **Estado del proyecto:** en desarrollo. Todavía no hay una versión descargable.
> Los valores de balance pueden cambiar antes del primer release.

---

## Qué es

Desert Mining Reborn lleva la minería al desierto. Bajo la arena hay vetas de mineral que no se
encuentran en ningún otro bioma: hay que cavar con pala, no con pico. Lo que sacas no es el mineral,
sino **fragmentos arenosos** que tienes que **tamizar** para convertirlos en recursos útiles.

El mod no reemplaza nada del juego base. Solo añade contenido nuevo y una entrada al tag de pala.

---

## Qué añade

### 5 menas arenosas

Aparecen únicamente en el bioma desierto, reemplazando arena.

| Mena | Da |
| :--- | :--- |
| Mena de oro arenosa | Fragmento de oro arenoso |
| Mena de redstone arenosa | Fragmento de redstone arenoso |
| Mena de lapislázuli arenosa | Fragmento de lapislázuli arenoso |
| Mena de diamante arenosa | Fragmento de diamante arenoso |
| Mena de esmeralda arenosa | Fragmento de esmeralda arenoso |

Dos cosas importantes que no verás en otros minerales:

- **Necesitas una pala de hierro o mejor.** Con una de madera o piedra romperás el bloque y no te
  dará nada.
- **Las menas caen.** Se comportan como arena: si minas el bloque de abajo, la veta se derrumba.
  No caves desde abajo de una veta expuesta.

### 5 fragmentos arenosos

El drop de las menas. No sirven por sí solos: son el ingrediente del tamizado.

### 5 tamices manuales

El tamiz **no se usa en el mundo**. Se usa en la **mesa de crafteo**: un fragmento + un tamiz
devuelve el recurso de verdad.

El tamiz no se consume, se **desgasta** un punto por cada tamizado. Cuando llega a cero, se rompe y
hay que fabricar otro. No se puede reparar.

| Tamiz | Tamizados |
| :--- | ---: |
| Tamiz manual de arena | 4 |
| Tamiz manual reforzado | 32 |
| Tamiz manual avanzado | 128 |
| Tamiz manual con punta de diamante | 512 |
| Tamiz manual de netherita | 2048 |

Los tres primeros se fabrican desde cero con materiales distintos. Los dos últimos se forjan en la
mesa de herrería partiendo del anterior.

### 3 palas

No se fabrican: se **forjan** en la mesa de herrería sobre una **pala de netherita** vanilla.

| Pala | Durabilidad | Velocidad | Efecto |
| :--- | ---: | ---: | :--- |
| Pala afortunada | 2031 | 9.0 | Probabilidad de fragmentos extra |
| Pala veloz | 2031 | 12.0 | Mina más rápido |
| Pala maestra | 4062 | 15.0 | Fragmentos extra, aún más rápido |

La pala maestra rompe arena en **un solo tick**. No es un truco: es lo que pasa con velocidad 15.0.

El bono de fragmentos se **suma** al de Fortuna de la loot table. Con Fortuna III y una pala maestra
puedes sacar bastantes más fragmentos de los que esperarías. Es intencional.

---

## Cómo se juega

```
Desierto
  └── minar menas con pala de hierro  →  fragmentos arenosos
        └── fabricar un tamiz
              └── tamizar fragmentos  →  recursos vanilla
                    └── fabricar tamices mejores
        └── fabricar mejoras de pala
              └── forjar una pala
                    └── forjar la pala maestra
```

El cuello de botella es la **mejora maestra**: exige haber fabricado antes la afortunada y la veloz,
más un bloque de netherita.

Sugerencia: consigue **Fortuna** en tu pala antes de ir al desierto. La diferencia se nota.

---

## Requisitos

| Componente | Versión |
| :--- | :--- |
| Minecraft | **1.20.1** |
| Forge | **47.4.28** o superior dentro de la rama 1.20.1 |
| Java | **17** |

---

## Instalación

Todavía no hay release. Cuando lo haya:

1. Instala Forge 1.20.1.
2. Copia el `.jar` del mod en tu carpeta `mods/`.
3. Arranca el juego.

En servidor, el mod debe estar en **cliente y servidor**.

---

## Créditos

El concepto original es de **Desert Mining**, publicado en CurseForge.

Este proyecto es una reimplementación independiente: **no reutiliza código, texturas, modelos,
sonidos ni nombres del original**. Todo el contenido de este repositorio se ha escrito y dibujado
desde cero a partir del comportamiento observable del mod.

Si te gustó la idea, apoya al autor original.

---

## Licencia

MIT. Se aplicará al publicar el primer release.

---

## Para desarrolladores

Si vas a tocar el código, construir el mod o crear contenido compatible, empieza por
[`docs/07-desarrollo.md`](docs/07-desarrollo.md).

| Documento | Contenido |
| :--- | :--- |
| [`docs/07-desarrollo.md`](docs/07-desarrollo.md) | Stack, build, estructura, convenciones, reglas clean-room |
| [`docs/01-contenido.md`](docs/01-contenido.md) | Catálogo de bloques e items con valores exactos |
| [`docs/02-interacciones.md`](docs/02-interacciones.md) | Cómo interactúa cada elemento con el juego base |
| [`docs/03-recetas.md`](docs/03-recetas.md) | Las 13 recetas |
| [`docs/04-worldgen.md`](docs/04-worldgen.md) | Generación de menas |
| [`docs/05-configuracion.md`](docs/05-configuracion.md) | Config del mod |
| [`docs/06-plan-implementacion.md`](docs/06-plan-implementacion.md) | Plan por fases |
