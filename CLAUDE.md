# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

An Android Studio / Gradle project built on the FIRST Tech Challenge (FTC) SDK for the **BIOBUZZ (2026-2027)** season. It controls a real competition robot (team Mega Snakes) via the FTC Driver Station + Control/Expansion Hub. There is no backend/web layer — all code runs on the robot's Android control hub. See `README.md` for the game rules (HIVE/CELL/FLOWER scoring); short version: the robot shoots POLLEN/NECTAR into a pivoting HIVE (CELLs tracked via AprilTags) and later scores at FLOWERs.

Two Gradle modules (see `settings.gradle`):
- **`FtcRobotController`** — the stock FTC SDK app module. Avoid modifying unless the task is explicitly about SDK-level behavior.
- **`TeamCode`** — the team's code, under `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/`. Almost all work happens here.

## Branches — check which one is checked out

`main` and `Test` diverged at `4b4ef90` and have **different layouts**; always check `git branch` before assuming a file exists.
- **`main`** — competition code: `TeleOperados/TeleOp_BLUE` / `TeleOp_RED`, Pedro Ivy autonomous routines under `Autonomo/pedro/Ivy/` (`Ivy_model.java` template, `RedDown/`, `RedUp/`, `BlueUp/`), `PIDS/`, `Another_Codes/`, and the core subsystems grouped under `Mech/ImportantMech/` (`Chassi`, `Feeder`, `FieldCentric`, `MidTake`, `Shooter`). Color sensor class there is `Mech/ColorSensor`.
- **`Test`** — reorganized experimentation branch: no TeleOps and **no `@Autonomous` OpModes at all** (Ivy routines, `PIDS/`, warm-up autos were removed); `ImportantMech/*` flattened into `Mech/`; `TunningShooters` moved to `Testes/`; color sensor class is `Mech/Color_Sensor`; adds LED drivers and many new test rigs. The `novo-autonomo` skill and `Ivy_model.java` only apply on `main`.

## Build / install

```bash
./gradlew :TeamCode:assembleDebug   # compile TeamCode APK (use before committing)
./gradlew :TeamCode:installDebug    # build + install on the connected Control Hub
```

- There is **no unit/instrumented test suite**. "Testing" means installing and running an OpMode on the physical robot from the Driver Station. Classes in `Testes/` are manual hardware rigs.
- Versions in `build.dependencies.gradle`: FTC SDK `12.0.0`, Pedro Pathing `core:3.0.0` / `revhub:3.0.0` / `telemetry:1.0.0` / `tuning:1.0.0`, Pedro Ivy `com.pedropathing.ivy:pedro:1.1.1`, Panels `com.bylazar:fullpanels:1.0.12`. The Pedro API changes a lot between major versions — write code against these exact versions, not memory of older ones.
- `build.common.gradle` (compileSdk 34, minSdk 24, targetSdk 28, Java 8) — don't edit; put customizations in `TeamCode/build.gradle`.

## Architecture

Organized by role; packages and identifiers mix Portuguese and English (follow the surrounding file). No DI/"robot container": every `Mech/*` class is a plain object created inside the OpMode, following `init(hardwareMap)` + per-loop methods (`drive(x,y,rx)`, `Activate(bool, ...)`, `update(...)`). Rising-edge button toggles always go through `TurnOn_TurnOff.IsActivate(now, last, state)`.

### Driving: two styles coexist
- **`Chassi`** — raw robot-centric mecanum on `leftFront/leftBack/rightFront/rightBack`, power-normalized, `rx` raised to the 4th power for fine turning.
- **`FieldCentric`** — newer style that drives **through the Pedro `Follower`** (`follower.manual(ManualDrive.fieldCentric(...))`), so odometry and path following share the same drivetrain. `resetHeading()` makes the current heading the driver's "forward". The OpMode must still call `follower.update()` every loop. Pedro's convention is strafe + = left, turn + = counter-clockwise, which is why callers flip signs on the gamepad values.
- **`Autonomo/pedro/AutoPath`** — TeleOp helper on the same `Follower`: a button rising-edge launches a path from the current pose to one of 8 fixed spots (`HiveTracking.StartPose` `*_PARK` / `*_MIDDLE`; `null` slots are ignored). Routes go around the field via the 4 corner nodes with Dijkstra + Liang-Barsky segment test so they never cross the central keep-out rectangle (`X_MIN..X_MAX`, `Y_MIN..Y_MAX` — a guessed box marked `shortcut:`); MIDDLE↔MIDDLE goes straight under the hive. Heading is held constant at the click pose. `HoldPosition` locks the robot in place. Stick input is the caller's override. `Testes/TestFieldAutoPath` is the reference composition (dpad = AutoPath, A = hold, B = field/robot toggle, options = reset heading).

### Shooting pipeline
- `Shooter` — velocity PIDF on `"shooter"` via the hub's `setVelocityPIDFCoefficients`; F is battery-compensated (`F = 16.67 × 12.97 / filteredVolts`, voltage sampled every 500 ms, coefficients re-sent only when F changes > 0.05). `Activate(on, targetVelocity)` takes the target from the caller. Optional `VoltageSensor` in the constructor, else it finds one in `init`. `Tune(...)` is for the tuning OpMode.
- `MidTake` — also a velocity-PIDF motor (`"midTake"`), `spin(btn1, btn2)` runs at fixed velocity. `Feeder` (`"feeder"`) is ±0.8 power; its encoder port is reused to read the hood's external encoder (`getHoodPos()`).
- `ValuesToShoot` — `TreeMap<distance (in, turret axis→hive), {hoodPos, motorVelocity}>` with floor/ceiling interpolation; the single place mapping distance to hood/shooter targets. `Double.NaN` = not measured yet — callers must hold the last good value (`HoodServo.hoodAjust` stops on NaN).
- `Servos/HoodServo` — CR servo `"hoodServo"` closed-loop with a PID on the external encoder ticks (deadband 15 ticks, integral clamp). `GateServo` (`"gateServo"`) opens once the shooter spun up. `FlowerServo`, `RampServo` are other actuators. `Servos/ToTurret/` is a superseded servo-turret experiment.

### Turret aiming (`Mech/Turret/`) — most involved logic
- `TurretConfig` — every tunable constant (gains `kP/kD/kS/kV`, limits, deadband, vision timing `maxStaleMs/lostTimeoutMs/confirmFrames`, `txSign`, `turretReversed`, plus `PoseCorrection` sub-block). Single place to retune.
- `TurretController` — owns `"turretMotor"`; power = P + static friction (both zeroed in deadband) + `kV`·target velocity + `kD`·(target vel − turret vel), battery-compensated, clamped, soft limits at `minDeg/maxDeg`. Angle is **unwrapped** (±400°), `setTarget` picks the nearest equivalent within limits.
- `TurretTracking` — `ODO`/`TRACK` estimator: in `TRACK` (after `confirmFrames` frames of the active CELL's tags) it low-pass-filters a bias between vision angle and odometry-predicted angle per tag, using turret angle history to compensate Limelight latency; on loss it falls back to `ODO` but **keeps the bias**. Also provides target velocity for the `kV` feedforward. Call `onPoseCorrected()` whenever `VisionPoseCorrector` applies a correction, or the bias is double-counted.
- `HiveTracking` — geometry/game state: `StartPose` enum (start poses plus `RED/BLUE_PARK` and `RED/BLUE_MIDDLE` field spots used by `AutoPath`), which CELL (up/down) to aim at with 3 in Y hysteresis, odometry turret angle and distance from the turret axis (24.8 cm ahead of robot center). Tag IDs: red 30–33/34–37, blue 38–41/42–45. Only `Limelight/hives_vermelhas.fmap` (red 30–37) exists — blue tracking is not validated end-to-end; tag positions in `TAG_POS` must match the `.fmap`.
- `VisionPoseCorrector` — corrects Pinpoint pose with Limelight MegaTag1 `botpose`, latency-compensated with a ~1 s history, converting camera→robot pose through the turret (camera is on the turret, so the Limelight's "Camera Pose in Robot Space" must stay all-zero). Rejects bad samples (see `Status` enum) and applies averaged corrections with a gain every `correctionIntervalMs`.
- `TurretTunning` (misspelled) — live tuning OpMode. `Testes/TestTrackingOnChassi` is the reference for composing turret + odometry + Limelight (A = odometry tracking, B = Limelight correction, X = apply pose correction).

### Sensing / feedback
- `LimeLight` — wraps `Limelight3A "limelight"`; pipelines 0/1 = red up/down, 2/3 = blue up/down. `TurnON(red, sideUp)` once in init, `setSide(...)` only sends on change, read `getResult()` once per loop, `getStatus()` is a blocking HTTP call (don't call every loop).
- `Color_Sensor` — REV Color Sensor V3 `"colorSensor"`; detects a ball by HSV hue/saturation window **or** distance < 50 mm, and `CountBall(inverted, ...)` counts rising edges 0–4 (decrements when `inverted`).
- `Mech/Led/` — two LED outputs: `RGB_Indicator` (goBILDA RGB indicator light driven as a `Servo "Indicator"`, fixed positions per color) and `Prism` (goBILDA Prism LED strip via the vendor I2C driver `GoBildaPrismDriver "prism"`; only re-sends over I2C when color/brightness changed, to avoid stalling the loop). `GoBildaPrismDriver`, `PrismAnimations`, `ColorPrism`, `Direction` are MIT-licensed vendor code from Base 10 Assets — don't refactor them. `Testes/TestLedColorSensor` shows the ball counter driving the indicator color.
- `Mech/c270/PollenDetector` — OpenCV `VisionProcessor` for a Logitech C270 (`"Webcam 1"`): HSV yellow mask → round blobs (splits touching balls with distanceTransform) → clusters (Union-Find) → dominant cluster `tx`. Runs on the camera thread and publishes an immutable `Result` via a `volatile` field; tunables are `public static` (persist across OpModes until app restart). Tested via `TestPollenTracking`.

### Odometry
- `Autonomo/pedro/Constants.java` — single source for `MecanumConfig`, `PinpointConfig`, `ForesightConfig`; `Constants.create(hardwareMap)` is the canonical way to get a `Follower`. `procedures/` + `Tuning.java` are Pedro's vendor tuning code.
- `Mech/PinpointConf` — separate Pinpoint localizer factory with **its own pod offsets**, used by code that doesn't go through a `Follower`; keep it in sync with `Constants` when retuning odometry.

## Conventions worth knowing

- `hardwareMap.get(..., "name")` names must exactly match the Driver Station robot configuration — mismatches compile fine and fail at runtime.
- Turret/tracking angles are in **degrees**; Pedro poses are radians (`Math.toRadians`/`toDegrees` at the boundary). Heading used for feedforward is unwrapped; use `normalize180`-style helpers only where wrapping is needed.
- Pedro `PoseFactory.mirrorX(axis)` / `mirrorY(axis)` require the axis argument in `core:3.0.0` (`mirrorX` also negates heading). A bare `.mirrorY()` doesn't compile.
- Experimental OpModes should get `@Disabled` before going to the competition branch, to keep the Driver Station list clean.

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
- Sempre usar o caveman, exceto quando for lhe pedido para desligar
