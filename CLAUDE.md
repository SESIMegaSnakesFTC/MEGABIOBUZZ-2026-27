# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

An Android Studio / Gradle project built on the FIRST Tech Challenge (FTC) SDK for the **DECODE (2025-2026)** season. It controls a real competition robot (team MegaSnakes/BIOBUZZ) via the FTC Driver Station + Control/Expansion Hub. There is no backend/web layer — all code runs on the robot's Android control hub.

Two Gradle modules (see `settings.gradle`):
- **`FtcRobotController`** — the stock FTC SDK app module. Avoid modifying unless the task is explicitly about SDK-level behavior; prefer putting robot logic in `TeamCode`.
- **`TeamCode`** — the team's actual code, under `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/`. This is where almost all work happens.

## Build / install commands

- `./gradlew build` — full build of both modules.
- `./gradlew :TeamCode:assembleDebug` — build just the TeamCode APK.
- `./gradlew :TeamCode:installDebug` — build and install onto a connected/ADB-attached Control Hub.
- There is **no unit/instrumented test suite**. "Testing" means deploying an OpMode and running it on the physical robot (or in the Driver Station app) — there is nothing to run headlessly. Classes under `Testes/` (and other `@Disabled`/experimental OpModes scattered through the tree) are manual hardware test rigs, not automated tests.
- Dependency/version config lives in `build.dependencies.gradle` (FTC SDK libs pinned to `12.0.0`, plus Pedro Pathing `com.pedropathing:core:3.0.0`, Pedro Ivy `com.pedropathing.ivy:pedro:1.1.1`, and `com.bylazar:fullpanels`; the Pedro API changes a lot between major versions, so write code against these versions). `build.common.gradle` holds shared Android build config (compileSdk 34, minSdk 24, targetSdk 28, Java 8 source/target) — per its own header comment, avoid editing it; put customizations in `TeamCode/build.gradle` instead.

## Architecture

Code under `teamcode/` is organized by role, not by season feature, and most packages mix Portuguese and English names (team is Brazilian). New comments go in Portuguese (see team rules below); class/method names follow whatever the surrounding file already uses.

- **`Mech/`** — thin hardware-subsystem wrappers, each owning one or more `hardwareMap` devices and exposing a small imperative API (`init(hardwareMap)`, then per-loop methods like `drive(x,y,rx)` or `Activate(boolean)`). Examples: `Chassi` (mecanum drive, power-normalized), `Shooter` (velocity-PIDF flywheel with battery-voltage feedforward compensation), `Feeder`/`midTake`/`FlowerServo`/`RampServo` (servo/motor actuators), `LimeLight` (wraps `Limelight3A`, pipeline switching per alliance/side), `IMU_Initializer`, `PinpointConf` (GoBilda Pinpoint odometry localizer factory), `TurnOn_TurnOff` (generic rising-edge toggle-latch helper used everywhere a gamepad button should flip a boolean state).
- **`Mech/Turret/`** — the turret aiming subsystem, the most involved piece of logic in the repo:
  - `TurretConfig` — all tunable constants (PID-ish gains `kP/kD/kS/kV`, angle limits, deadband, vision/tracking timing like `maxStaleMs`/`lostTimeoutMs`/`confirmFrames`). Treat as the single place to retune turret behavior.
  - `TurretController` — owns the turret `DcMotorEx`; converts an angle target into motor power using proportional + static-friction + velocity-damping + chassis-rotation-feedforward terms, with battery-voltage compensation and soft limits at `minDeg`/`maxDeg`. Degrees are tracked unwrapped (can exceed ±360) so `setTarget` picks the nearest equivalent angle within range.
  - `TurretTracking` — a 2-state (`ODO` / `TRACK`) estimator that fuses chassis odometry heading with Limelight AprilTag detections: while a target hive's tag is visible it low-pass-filters a bias between vision-measured angle and odometry-predicted angle; when vision is lost/stale it falls back to pure odometry. Also estimates chassis angular velocity from heading deltas for the controller's feedforward term.
  - `HiveTracking` — pure geometry/game-state helper: given alliance + starting pose, tracks which of the two "hives" (upper/lower) the robot should currently aim at (with Y-axis hysteresis to avoid flip-flopping near the midline), and computes the odometry-only turret angle and distance to the active hive. Also owns the AprilTag ID sets per hive/alliance (some still `TODO` for blue).
  - `TestingAtritServo`, `TrackingTurretServo`, `TurretTunning`, `TurretServo` (in `Mech/`) — earlier/parallel servo-driven turret experiments; the motor-driven `TurretController`/`TurretTracking` pair above is the current direction (see recent commit history).
- **`Testes/TestTrackingOnChassi.java`** is the best current reference for how the turret subsystem composes with everything else: it wires `Chassi` + `PinpointConf`/Pedro `PinpointLocalizer` + `LimeLight` + `HiveTracking` + `TurretController` + `TurretTracking` together in one TeleOp loop (gamepad A toggles odometry-only tracking, B toggles Limelight-assisted correction).
- **`pedro/`** — integration with the third-party **Pedro Pathing** library (`com.pedropathing:*`) for localization/path following, autonomous.
  - `Constants.java` is the single source of truth for drivetrain (`MecanumConfig`), localizer (`PinpointConfig`), and motion-profile (`ForesightConfig`) tuning values, and exposes `Constants.create(hardwareMap)` to build a `Follower`. This is the canonical path to get a configured `Follower`, in preference to constructing one by hand.
  - `procedures/` (`PinpointTuner`, `MecanumTuner`, `ForesightTuner`, `OTOSTuner`, `ThreeWheelTuner`, etc.) are Pedro Pathing's own auto-tuning `Procedure`/`TuningOpMode` boilerplate, generated/structured by the library's tuning tool — treat as mostly vendor code; `Tuning.java` is the entry OpMode that runs them.
  - `Others/` (and `Others/Ivy/`) hold actual autonomous routines per alliance/starting position. The established pattern (see `AzulBaixoMov.java`): a package-private `Path<Name>` class defines waypoint `Pose`s and builds `Path` segments, and the `@Autonomous` `LinearOpMode` class gets a `Follower` from `Constants.create(...)`, sets the starting pose, then calls each path via a local `seguirPath(Path)` helper that loops `follower.update()` with a timeout. Most of these OpModes are currently `@Disabled` while in progress.
  - `Others/Ivy/` is the newer, active autonomous style built on **Pedro Ivy** (`com.pedropathing.ivy:pedro`), a command scheduler. Template is `Ivy/Model.java`: an iterative `OpMode` (not `LinearOpMode`) that calls `Scheduler.reset()` + `Constants.create(...)` + `setPose` in `init()`, schedules a `sequential(follow(follower, path), waitMs(...), ...)` command group in `start()`, and runs `follower.update(); Scheduler.execute();` in `loop()`. Poses come from `PoseFactory.degrees()` and paths from the static `Paths.line(...)` builders. Real routines live in one subfolder per starting position (e.g. `Ivy/RedDown/` with `RedDown`, `RedFlower`, `SoloRedDown`), and path definitions still live in a package-private companion class per file (e.g. `PathsRedDown`). Prefer this style for new autonomous routines.
  - `WarmUp_WithoutPedro/` — simpler autonomous variants that don't depend on Pedro Pathing (direct encoder/time-based moves), used as warm-up/fallback routines.
- **`PIDS/`** — `RobotPID.java` is a standalone heading PID controller (not tied to Pedro Pathing or the turret), for use in simpler/manual control loops; `TunningShooters.java` is a TeleOp for live-tuning the shooter's velocity PIDF.
- **`Another_Codes/Movimento.java`** — a plain mecanum TeleOp over `Chassi`, using `LynxModule` bulk caching.
- **`TeleOpWarmUp/`**, **`c270/`** — in-progress or scratch OpModes (several `@Disabled`); `c270/Functions.java` is an AprilTag-via-webcam experiment using the stock FTC `VisionPortal`/`AprilTagProcessor` rather than Limelight.

## Conventions worth knowing

- Hardware device names passed to `hardwareMap.get(Class, "name")` (e.g. `"leftFront"`, `"turretMotor"`, `"pinpoint"`, `"limelight"`, `"shooter"`) must match the robot's physical hardware configuration on the Driver Station/Control Hub exactly — renaming one in code without updating the on-robot config (or vice versa) breaks the OpMode at runtime, not at compile time.
- Angles throughout the turret/tracking code are in **degrees**, field heading is **unwrapped** (continuously accumulated, not wrapped to ±180) where used for feedforward, and `normalize180`-style helpers are used when wrapping is actually needed — keep this distinction when touching that code.
- Subsystem classes follow `init(hardwareMap)` + stateless-ish per-loop update methods; they're plain objects instantiated directly in OpModes, not a DI/subsystem-manager framework — there's no shared robot "container" class to register into.

## Regras da equipe

- Sempre responder em português, de forma direta e clara.
- Comentários no código em português, curtos e claros.
- Antes de realizar uma alteração no código, dê ao menos uma boa ideia de implementação (caso haja).
- Sempre que eu passar uma tarefa e não indicar um arquivo, lembre-me.
- Sempre peça permissão antes de editar o código ou implementar mudanças.
- Após uma alteração, faça um breve resumo das alterações, em tópicos enumerados de forma objetiva.
- Mensagens de commit: título curto + corpo dividido em partes enumeradas (incluir só as que se aplicam):
  1. `Add:` arquivos/classes novos adicionados
  2. `Fix:` problemas corrigidos e em quais arquivos
  3. `Implement:` lógica nova implementada e em quais classes
  4. `Refactor:` arquivos movidos/renomeados, reorganização sem mudar lógica
- Antes de commitar, propor a mensagem nesse formato e aguardar aprovação; só depois fazer o push.
