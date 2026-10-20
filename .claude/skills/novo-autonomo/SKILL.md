---
name: novo-autonomo
description: Use ao criar ou editar um autônomo Pedro Ivy (Autonomo/pedro/Ivy/) — padrão de arquivo, ciclo init/start/loop e mecanismos.
---

**`Ivy/Ivy_model.java` (`@Disabled`) é o template a copiar** ao criar um novo autônomo — diferente de um esqueleto só de path, ele já liga o loop completo de mecanismos (turret, shooter, feeder, gate, hood, `VisionPoseCorrector`) do mesmo jeito que as rotinas reais, então copie em vez de montar a fiação do zero.

Padrão:
- Uma classe package-private `<Nome>Path` no **mesmo arquivo** define os `Pose`s de waypoint (via `PoseFactory.degrees()`) e monta os `Path`s com `com.pedropathing.api.Paths.line/curve`.
- A classe pública `@Autonomous` (um `OpMode` iterativo, não `LinearOpMode`):
  - `init()`: `Scheduler.reset()` + `Constants.create(...)` + `follower.setPose(...)`.
  - `start()`: agenda a árvore de comandos (`sequential`/`parallel` de `follow(follower, path)`, `waitMs`, `instant(() -> ...)`, `waitUntil(...)`, `hold(follower)`).
  - `loop()`: `follower.update(); Scheduler.execute(); atualizarMecanismos();` — os mecanismos atualizam todo loop, independente do scheduler, guiados por flags booleanas que o schedule troca via `instant(...)`.
