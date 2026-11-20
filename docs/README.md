# Documentación — Desert Mining Reborn

## Qué es este proyecto

Reimplementación **clean-room** del mod **Desert Mining** (Forge 1.20.1, v1.0.0) sobre
**Forge 1.20.1**. Se replica la funcionalidad observable; el código, los nombres finales y los
assets son propios.

## Convención de marcado (leer antes de todo)

Cada afirmación técnica de esta documentación lleva una marca de origen:

| Marca | Significado |
| :--- | :--- |
| **[R]** | **Replicado**: comportamiento verificado leyendo el JAR y el descompilado del original. |
| **[P]** | **Propuesta**: decisión nuestra, mejora o desvío consciente. No viene del original. |
| **[V]** | **A verificar**: comportamiento inferido que debe confirmarse al implementar. |

Si algo no tiene marca, es contexto general. **No inventar valores sin marca [R] o [P].**

## Estado del proyecto

| Ítem | Estado |
| :--- | :--- |
| Análisis funcional del original | completo (`docs/01` – `docs/04`) |
| Plan de implementación | escrito (`docs/06`) |
| Proyecto Gradle | **no existe todavía** — bloquea todo |
| Package root | `com.rexthar.desertminingreborn` — definido |
| Assets | los provee el usuario, pendiente de aprobación |

## Índice

| Documento | Contenido |
| :--- | :--- |
| [`01-contenido.md`](01-contenido.md) | Bloques y items: valores exactos, propiedades, comportamiento |
| [`02-interacciones.md`](02-interacciones.md) | Cómo interactúa cada elemento con vanilla y entre sí |
| [`03-recetas.md`](03-recetas.md) | Todas las recetas de crafteo y forja |
| [`04-worldgen.md`](04-worldgen.md) | Generación de menas en el desierto |
| [`05-configuracion.md`](05-configuracion.md) | Config del mod (propuesta) |
| [`06-plan-implementacion.md`](06-plan-implementacion.md) | Plan por fases con criterios de aceptación |

Documentos de planeación en la raíz del repo:

| Documento | Contenido |
| :--- | :--- |
| `AGENTS.md` | Reglas del proyecto, stack, prohibiciones legales |
| `openspec/project.md` | Contexto SDD, matriz funcional resumida |
| `openspec/changes/0001-forge-reimplementation/proposal.md` | Proposal SDD |

## Reglas que atraviesan toda la documentación

1. **Nunca** copiar código, texturas, modelos, sonidos, nombres ni estructura de clases del original.
2. `recursos/` es **solo referencia**. Nunca se importa desde ahí al proyecto.
3. Ningún build roto pasa a la siguiente fase.
4. Nada de assets sin aprobación explícita del usuario.
5. Nada de publicación sin autorización explícita.

## Decisiones abiertas

| ID | Decisión | Bloquea |
| :--- | :--- | :--- |
| **D1** | ~~Package root `com.<usuario>.desertminingreborn`~~ **RESUELTO**: `com.rexthar.desertminingreborn` | — |
| **D2** | Assets: origen y aprobación | Fase G |
| **D3** | ¿Las menas conservan la gravedad? | Fase B |

**D3** merece discusión: el original hereda de `FallingBlock` **[R]**, así que las menas caen como
arena. Es parte de su identidad ("minerales compuestos de arena"), pero es una fuente de bugs
conocida. Ver `docs/02-interacciones.md` §1.3 antes de decidir.
