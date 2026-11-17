# 🐍 MEGA SNAKES: Repositório Oficial de Programação - Temporada BIOBUZZ 🤖

## Módulo TeamCode: Conteúdo do Repositório

Este repositório contém o código-fonte principal do robô da equipe **Mega Snakes** na **Temporada BIOBUZZ™ (FIRST® Tech Challenge 2026–2027)**. O módulo `TeamCode` é onde todo o nosso desenvolvimento acontece. O restante do projeto é a estrutura padrão do SDK da FTC, incluindo os recursos de aprendizado.

> Este README descreve **o que está no código hoje**. Quando um trecho for uma **inferência** (algo deduzido do código, não declarado nele), ele aparece marcado como _(inferência)_.

---

## Sumário

1. [🌼 A Temporada BIOBUZZ](#1--a-temporada-biobuzz)
2. [⚙️ Códigos de Produção da Equipe](#2-️-códigos-de-produção-da-equipe)
3. [🏗️ Arquitetura do Robô](#3-️-arquitetura-do-robô)
4. [🚀 Shooter](#4--shooter)
5. [🎯 Turret e Tracking](#5--turret-e-tracking)
6. [👁️ Limelight](#6-️-limelight)
7. [🛣️ Autônomos](#7-️-autônomos)
8. [🔧 Atuadores, Sensores e LEDs](#8--atuadores-sensores-e-leds)
9. [🗂️ Estrutura de Pastas](#9-️-estrutura-de-pastas)
10. [💻 Instalação e Compilação](#10--instalação-e-compilação)
11. [📚 Recursos de Exemplo da FTC](#11--recursos-de-exemplo-da-ftc)
12. [📝 Convenções de Código](#12--convenções-de-código)
13. [🤝 Como Contribuir e Solucionar Problemas](#13--como-contribuir-e-solucionar-problemas)

---

### 1. 🌼 A Temporada BIOBUZZ

**BIOBUZZ™ presented by RTX** é o jogo da FTC 2026–2027 e faz parte da temporada **FIRST® CANOPY™**, que tem como tema a biodiversidade. O resumo abaixo vem do [Competition Manual oficial](https://ftc-resources.firstinspires.org/ftc/game/manual), versão TU04. **Em caso de dúvida, o manual sempre vale mais que este README.**

**Formato da partida:** 2 alianças, com 2 equipes cada, coletam **POLLEN** e **NECTAR**, **lançam** esses elementos na **HIVE** da própria aliança e os colocam nas **FLOWERS**.

| Período | Duração | O que acontece |
| :--- | :--- | :--- |
| **AUTO** | 30 s | O robô age sozinho: sai da parede (LEAVE), lança na HIVE e estaciona. |
| Transição | 8 s | Intervalo entre AUTO e TELEOP. |
| **TELEOP** | 2 min | Os pilotos controlam o robô. Faltando **60 s**, liberam-se a pontuação nas FLOWERS e a entrada de todo o NECTAR restante. |

**Elementos de jogo:**

| Elemento | Descrição |
| :--- | :--- |
| **POLLEN** | Bolas amarelas de ~2,8 in (7,1 cm). São 40 por partida. |
| **NECTAR** | Bolas vermelhas ou azuis de ~3,6 in (9,1 cm). São 8 de cada cor por partida. |
| **HIVE** | Estrutura no centro do campo, com uma HIVE vermelha e uma azul. Cada HIVE tem **2 CELLS**, uma em cada ponta, e gira num pivô. Quando entram elementos suficientes na CELL virada para cima, a HIVE **tomba (TIP)**. Cada TIP pontua e libera mais NECTAR para a aliança. |
| **CELL / AprilTags** | Embaixo de cada CELL há um cluster com **4 AprilTags** (família 36h11). Elas servem para navegação e mira. |
| **FLOWER** | São 4, presas à parede do campo. O topo recebe POLLEN e NECTAR, e a abertura de baixo permite retirar POLLEN. |
| **GARDEN** | Zona de cada aliança, que pontua para a aliança da mesma cor. |

**Pontuação (Tabela 10-2 do manual):**

| Ação | AUTO | TELEOP |
| :--- | :---: | :---: |
| LEAVE | 3 | – |
| PARK (na LOADING ZONE) | 5 | 5 |
| HIVE TIP | 20 | 20 |
| POLLEN/NECTAR que sobrou na CELL | – | 2 |
| Bottom NECTAR Bonus (FLOWER) | – | 5 |
| POLLEN/NECTAR numa FLOWER que a aliança domina | – | 2 |
| POLLEN/NECTAR no GARDEN | – | 1 |

**Ranking Points:** WIN = 3, TIE = 1, além de **SWARM** (LEAVE + PARK ≥ 16 pts), **POLLINATOR 1** (≥ 4 TIPs) e **POLLINATOR 2** (≥ 7 TIPs). Esses limites valem para eventos comuns. Os do Regional e do Championship ainda serão anunciados (TBA).

**Por que isso importa para a programação** _(inferência)_: o TIP vale 20 pontos e conta para dois RPs. Por isso o foco técnico atual do código é **lançar com precisão na CELL de cima de forma contínua**, com shooter de velocidade controlada e turret que mira sozinha usando odometria e as AprilTags das CELLS.

> 📊 **Análise de partidas:** ainda não há vídeos, logs, telemetria salva ou relatórios de partidas neste repositório, então não registramos resultados aqui.

---

### 2. ⚙️ Códigos de Produção da Equipe

Estes são os OpModes e as classes que a equipe Mega Snakes desenvolveu para controlar o robô.

| Categoria | Descrição e Objetivo |
| :--- | :--- |
| **Mecanum Drive** | `Mech/Chassi`: tração holonômica robot-centric com normalização de potência. |
| **Field-Centric** | `Mech/FieldCentric`: movimentação pela perspectiva do piloto, feita pelo `Follower` do Pedro. |
| **AutoPath** | `Autonomo/pedro/AutoPath`: no TeleOp, um botão leva o robô sozinho até locais fixos do campo, contornando o meio. |
| **Shooter** | `Mech/Shooter`: flywheel com PIDF de velocidade e compensação de bateria. |
| **Turret com Tracking** | `Mech/Turret/`: turret movida a motor que mira na HIVE usando a odometria Pinpoint e corrige a mira com a Limelight. |
| **Integração Limelight** | `Mech/LimeLight`: troca de pipeline por aliança/HIVE e leitura das AprilTags. |
| **Sensor de cor + LEDs** | `Mech/Color_Sensor` conta as bolas que passam; `Mech/Led/` mostra o estado no indicador RGB ou na fita Prism. |
| **Visão de POLLEN** | `Mech/c270/PollenDetector`: OpenCV na webcam C270 que acha o grupo de pólens amarelos. |
| **Pedro Pathing** | `Autonomo/pedro/`: odometria, seguimento de trajetórias e tuning. |
| **Atuadores** | `Feeder`, `MidTake`, `HoodServo`, `GateServo`, `FlowerServo`, `RampServo`. |
| **Ferramentas de Tuning** | OpModes para ajustar shooter/midTake, turret, sensor de cor e o Pedro Pathing. |

---

### 3. 🏗️ Arquitetura do Robô

O código não usa um framework de "subsystems". Cada mecanismo é uma **classe simples** que segue sempre o mesmo padrão:

1. É criada direto no OpMode (`new Chassi()`).
2. Recebe `init(hardwareMap)` (ou o motor no construtor) para pegar o hardware.
3. Recebe a cada loop um método de ação (`drive(x, y, rx)`, `Activate(bool, ...)`, `update(...)`).

```
                ┌──────────────┐     pose (x, y, heading)    ┌───────────────┐
 Pinpoint ─────►│ PinpointConf │────────────────────────────►│  HiveTracking │──► ângulo pela odometria
 (odometria)    └──────────────┘                             │ (geometria)   │    + qual CELL mirar
                       ▲                                     └───────┬───────┘
                       │ correção de pose                            │
                ┌──────┴───────────────┐   AprilTags (tx)    ┌───────▼────────┐
 Limelight ────►│ VisionPoseCorrector  │◄────────────────────│ TurretTracking │──► ângulo-alvo + velocidade do alvo
 (LimeLight)    └──────────────────────┘                     │  (ODO / TRACK) │
       └─────────────────────────────────────────────────────►└───────┬────────┘
                                                                     │
                                                             ┌───────▼─────────┐
                                                bateria ────►│TurretController │──► motor "turretMotor"
                                                             └─────────────────┘
```

O OpMode que melhor mostra essas peças juntas é o [`Testes/TestTrackingOnChassi.java`](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Testes/TestTrackingOnChassi.java) (`TrackingOnChassi` no Driver Station):

| Controle (gamepad1) | Função |
| :--- | :--- |
| Analógicos | Movimentação mecanum (`Chassi.drive`). |
| **A** (liga/desliga) | Tracking da turret pela odometria. |
| **B** (liga/desliga) | Ajuste fino da mira pelo `tx` das AprilTags. |
| **X** (liga/desliga) | Aplica a correção de pose pela visão. Desligado, só mede e mostra na telemetria. |
| RT / LT | Giro manual da turret quando o tracking está desligado. |

A posição inicial é escolhida na constante `START` (`RED_DOWN`, `RED_UP`, `BLUE_DOWN`, `BLUE_UP`). O OpMode também ativa o **bulk caching** do `LynxModule`, que junta as leituras do Hub numa só por loop.

#### Movimentação: robot-centric e field-centric

Existem duas formas de mover o robô:

| Classe | Como funciona |
| :--- | :--- |
| `Mech/Chassi` | Mecanum **robot-centric** direto nos 4 motores. O giro (`rx`) é elevado à 4ª potência para dar precisão em giros lentos. |
| `Mech/FieldCentric` | Mecanum **field-centric**: o stick segue a visão do piloto, qualquer que seja o heading. Manda a potência **pelo `Follower` do Pedro** (`follower.manual(...)`), então odometria e paths usam o mesmo drivetrain. `resetHeading()` define a "frente" do piloto. O OpMode continua chamando `follower.update()` todo loop. |

> No Pedro, lateral positivo = **esquerda** e giro positivo = **anti-horário**. Por isso os OpModes invertem alguns sinais do gamepad.

#### AutoPath: ir sozinho até um local do campo

📄 [`Autonomo/pedro/AutoPath.java`](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/pedro/AutoPath.java)

- `GoToPosition(b1..b8)`: na borda de subida de um botão, cria um path da pose atual até um dos **8 locais fixos**. Hoje estão preenchidos `RED_PARK`, `BLUE_PARK`, `RED_MIDDLE` e `BLUE_MIDDLE` (de `HiveTracking.StartPose`); os slots `null` são ignorados.
- A rota passa pelos **4 cantos** do campo e nunca cruza o retângulo central proibido (estruturas + margem). A menor rota é achada com **Dijkstra**, e cada reta é testada contra o retângulo com **Liang-Barsky**. Entre os dois `MIDDLE` o robô vai reto, por baixo da HIVE.
- O heading fica travado no valor do momento do clique, e todos os trechos viram **um path só** (não para nos cantos).
- `HoldPosition(...)`: trava o robô na posição.
- Quem decide o override é o OpMode: mexer o stick interrompe o path.

> ⚠️ O retângulo proibido (`X_MIN..X_MAX`, `Y_MIN..Y_MAX`) ainda é um chute; trocar pelas medidas reais.

OpMode de referência: [`Testes/TestFieldAutoPath.java`](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Testes/TestFieldAutoPath.java) (`AutoPath&FieldCentric`):

| Controle (gamepad1) | Função |
| :--- | :--- |
| D-pad ← → ↓ ↑ | Vai até `RED_PARK`, `BLUE_PARK`, `RED_MIDDLE`, `BLUE_MIDDLE` |
| **A** (liga/desliga) | Trava a posição atual |
| **B** (liga/desliga) | Alterna FIELD / ROBOT-centric |
| options | Reseta a "frente" do piloto |
| Analógicos | Movimentação manual (interrompe o path) |

---

### 4. 🚀 Shooter

📄 [`Mech/Shooter.java`](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Mech/Shooter.java) usa o motor `"shooter"` (`DcMotorEx`).

- **PIDF de velocidade:** usa o controle de velocidade do próprio Hub (`setVelocityPIDFCoefficients`) com **P = 60**, I = D = 0 e **F base = 16.67**. A velocidade-alvo vem de quem chama: `Activate(ligado, velocidadeAlvo)`.
- **Compensação de bateria:** com a bateria baixa, o motor rende menos para a mesma potência. Por isso o termo F é ajustado pela tensão:

  ```
  F = 16.67 × (12.97 / tensão_filtrada)
  ```

  A tensão é lida a cada **500 ms**, porque a leitura no Hub é lenta. Leituras abaixo de 7 V são ignoradas e as outras passam por um filtro (`0.8·antiga + 0.2·nova`) para suavizar as quedas ao acelerar. Os coeficientes só são reenviados quando o F muda mais que **0.05**.
- `Activate(true, v)` liga o shooter na velocidade `v` e `Activate(false, v)` zera a velocidade.
- O `VoltageSensor` pode ser passado no construtor; se não for, o `init` busca um sozinho.

**Tuning:** [`Testes/TunningShooters.java`](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Testes/TunningShooters.java) (`Shooter PIDF Tuning`) ajusta ao vivo o P e o F do **shooter** (gamepad1) e do **midTake** (gamepad2) pelo D-pad, com a mesma velocidade-alvo para os dois (A/Y mudam ±20). Usa os métodos `Tune(...)` das classes.

O `MidTake` também é controlado por velocidade (PIDF do Hub), não mais por potência fixa.

---

### 5. 🎯 Turret e Tracking

A turret é o subsistema mais elaborado do repositório. A versão atual é **movida a motor** (`"turretMotor"`). As classes com servo (`TurretServo`, `TrackingTurretServo`, `TestingAtritServo`, `TurretTunning`) são experimentos anteriores ou paralelos.

#### `TurretConfig`: todas as constantes de ajuste

📄 [`Mech/Turret/TurretConfig.java`](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Mech/Turret/TurretConfig.java). É **o único lugar** para retunar a turret.

| Grupo | Constantes (valor atual) |
| :--- | :--- |
| Mecânica | `ticksPerRev = 28`, `gearRatio = 18.88` (≈ 1,47 ticks por grau) |
| Controle | `kP = 0.0145`, `kD = 0.00011`, `kS = 0.047`, `kV = 0.0008`, `maxPower = 0.9`, `nominalV = 12.0` |
| Limites | `minDeg = -400`, `maxDeg = 400`, `deadbandDeg = 1.0` |
| Visão | `alpha = 0.6`, `lockToleranceDeg = 2`, `maxStaleMs = 110`, `lostTimeoutMs = 300`, `confirmFrames = 3`, `txSign = -1` |
| Correção de pose | subclasse `PoseCorrection`: intervalo, ganhos e filtros do `VisionPoseCorrector` |

#### `TurretController`: do ângulo-alvo à potência do motor

📄 [`Mech/Turret/TurretController.java`](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Mech/Turret/TurretController.java)

A potência enviada ao motor é a soma de quatro termos:

```
potência = kP·erro + kS·sinal(erro)        ← proporcional + atrito estático (zerados dentro da deadband)
         + kV·velocidade_do_alvo           ← feedforward: acompanha o alvo se movendo
         + kD·(vel_alvo − vel_turret)      ← amortece só a diferença de velocidade
```

Depois disso, o controlador:

1. **Compensa a bateria**, multiplicando por `12 V / tensão`.
2. **Limita** a potência em ±`maxPower`.
3. Aplica os **limites suaves**: não empurra além de `minDeg`/`maxDeg`.

O ângulo é **unwrapped**, ou seja, pode passar de ±360°. Assim, `setTarget` escolhe o equivalente do alvo (±360·k) **mais próximo da posição atual** dentro dos limites, e a turret não dá a volta inteira à toa. Com `enabled = false`, o controlador aceita potência manual (`manualPower`) e mantém os limites.

#### `TurretTracking`: estimador ODO / TRACK

📄 [`Mech/Turret/TurretTracking.java`](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Mech/Turret/TurretTracking.java)

- **Estado `ODO`:** a mira vem só da odometria, pelo ângulo calculado na `HiveTracking`.
- **Estado `TRACK`:** entra quando as tags da CELL rastreada aparecem por `confirmFrames` frames seguidos. Para cada tag vista, o estimador:
  1. Compara o ângulo medido pela visão com o ângulo esperado **daquela tag**, considerando o ângulo da turret **no instante da foto**. Para isso guarda um histórico e desconta a latência da Limelight.
  2. Tira a média dessas diferenças, que formam o **bias** (o quanto a odometria está errada).
  3. Suaviza o bias com um filtro passa-baixa (`alpha`).
- **Perda de visão:** quando a visão some ou fica velha (`maxStaleMs`) por mais de `lostTimeoutMs`, o estimador volta para `ODO`. **O bias continua aplicado**, então a odometria fica "recalibrada".
- **Saídas:**
  - alvo final = `ânguloOdometria + bias`;
  - `getTargetVelocity()`, que é a derivada filtrada do ângulo da odometria e alimenta o feedforward `kV` do controller.
- `onPoseCorrected()` zera o bias quando o `VisionPoseCorrector` corrige a pose, para a correção não ser aplicada duas vezes.

#### `HiveTracking`: geometria do campo

📄 [`Mech/Turret/HiveTracking.java`](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Mech/Turret/HiveTracking.java)

- **Posições iniciais** (`StartPose`), em polegadas e graus no referencial do Pinpoint/Pedro:

  | StartPose | Aliança | x | y | heading |
  | :--- | :--- | :---: | :---: | :---: |
  | `RED_DOWN` | Vermelha | 56 | 9 | 90° |
  | `RED_UP` | Vermelha | 56 | 132.5 | −90° |
  | `BLUE_DOWN` | Azul | 82 | 9 | 90° |
  | `BLUE_UP` | Azul | 84 | 132.5 | −90° |
  | `RED_PARK` | Vermelha | 15 | 95 | 0° |
  | `RED_MIDDLE` | Vermelha | 70.7 | 117.7 | 0° |
  | `BLUE_PARK` | Azul | 130 | 45.5 | 180° |
  | `BLUE_MIDDLE` | Azul | 70.7 | 23.5 | 0° |

  Os `*_PARK` e `*_MIDDLE` não são posições iniciais: são locais do campo usados pelo `AutoPath`.

- **Qual alvo mirar:** cada aliança tem dois pontos de mira, "cima" (`UP`) e "baixo" (`DOWN`). O alvo troca conforme o Y do robô passa da linha média entre eles, com **histerese de 3 in** para não ficar alternando perto da linha. _(Inferência: `UP`/`DOWN` correspondem às duas CELLS da HIVE da aliança, já que cada CELL tem seu próprio cluster de AprilTags.)_
- **AprilTags:** vermelho `DOWN` = **30–33**, vermelho `UP` = **34–37**, azul `DOWN` = **38–41** e azul `UP` = **42–45**. Ainda falta validar o tracking azul em campo e criar o field map azul.
- **Offset da turret:** o eixo da turret fica **24,8 cm à frente** do centro do robô (`TURRET_OFFSET_X_IN`), e os ângulos e distâncias são calculados a partir desse eixo.
- **Para o shooter:** `distanceFromTurretToHive(...)` dá a distância do eixo da turret até o alvo.

#### Odometria Pinpoint

- 📄 [`Mech/PinpointConf.java`](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Mech/PinpointConf.java) cria um `PinpointLocalizer` do Pedro (dispositivo `"pinpoint"`) com os offsets dos pods. É o que a TeleOp de tracking usa.
- 📄 `Autonomo/pedro/Constants.java` tem a **sua própria** `PinpointConfig`, usada pelo `Follower` dos autônomos, com offsets diferentes do `PinpointConf`.

#### `VisionPoseCorrector`: correção da pose pela visão

📄 [`Mech/Turret/VisionPoseCorrector.java`](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Mech/Turret/VisionPoseCorrector.java)

Usa o **botpose** (MegaTag1) da Limelight para corrigir a pose do Pinpoint:

1. Compensa a latência e converte a pose da câmera para a pose do robô, passando pela turret, porque a câmera está montada nela (`cameraOnTurret = true`).
2. Filtra as medidas ruins: idade, distância, robô ou turret em movimento, yaw incoerente, saltos.
3. Acumula as medidas boas e aplica a média a cada `correctionIntervalMs`, com ganho `positionGain`.

> A "Camera Pose in Robot Space" da Limelight deve ficar **toda em zero**, porque a conta da turret é feita no código.

---

### 6. 📷️ Limelight

📄 [`Mech/LimeLight.java`](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Mech/LimeLight.java) usa o `Limelight3A` `"limelight"` com poll rate de 90 Hz.

| Pipeline | Aliança | Alvo |
| :---: | :--- | :--- |
| 0 | Vermelha | Cima (`UP`) |
| 1 | Vermelha | Baixo (`DOWN`) |
| 2 | Azul | Cima (`UP`) |
| 3 | Azul | Baixo (`DOWN`) |

- `TurnON(red, sideUp)`: liga a câmera já no pipeline certo. Chamar **uma vez no init**.
- `setSide(red, sideUp)`: troca o pipeline quando o alvo da `HiveTracking` muda. Só envia o comando se o pipeline mudou de fato.
- `getResult()`: devolve o último `LLResult`. Leia **uma vez por loop**.
- `getStatus()`: é uma requisição HTTP bloqueante (até ~100 ms), então **não** chame em todo loop.
- `MediumTx()`: média do `tx` de todas as tags vistas.

**Field map:** [`Limelight/hives_vermelhas.fmap`](Limelight/hives_vermelhas.fmap) tem as posições das tags **30–37** (tamanho 80,3 mm) e deve ser carregado na Limelight. Os valores precisam bater com o `TAG_POS` da `HiveTracking`, então **mude os dois juntos**. Ainda não existe field map para as tags azuis.

---

### 7. 🛣️ Autônomos

Os autônomos usam **Pedro Pathing**, uma biblioteca de seguimento de trajetórias com curvas Bézier e localização por odometria. O `Follower` configurado sai sempre de `Constants.create(hardwareMap)` ([`Autonomo/pedro/Constants.java`](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/pedro/Constants.java)), que junta o localizador Pinpoint, o drivetrain Mecanum e o controlador de movimento `Foresight`.

> ⚠️ **Depende da branch.** Na branch `Test` **não há nenhum `@Autonomous`**: as rotinas Ivy foram retiradas na reorganização. Elas continuam na branch `main`.

#### Pedro Ivy (branch `main`)

**Pedro Ivy** é um agendador de comandos: ele permite descrever o autônomo como uma sequência de ações (`sequential(...)`) em vez de laços manuais. O modelo a copiar é o `Autonomo/pedro/Ivy/Ivy_model.java`:

```java
// init():  Scheduler.reset(); follower = Constants.create(hardwareMap); follower.setPose(start);
// start(): schedule(sequential(follow(follower, path1), waitMs(600), follow(follower, path2)));
// loop():  follower.update(); Scheduler.execute(); atualizarMecanismos();
```

| Pasta (em `Ivy/`) | Rotinas |
| :--- | :--- |
| `RedDown/` | `RedDownMech`, `RedFlower`, `SoloRedDownMech`, `SoloRedDown` |
| `RedUp/` | `RedUpMech` |
| `BlueUp/` | `BlueUpMech` |

- Sufixo `*Mech` = o loop completo de mecanismos (turret, shooter, feeder, gate) roda junto com o path. Sem sufixo = só movimento.
- **Seleção de rota:** não existe seletor automático. Cada posição ou estratégia é um **OpMode separado**, escolhido no Driver Station.

#### Ferramentas do Pedro (as duas branches)

| Arquivo | Conteúdo |
| :--- | :--- |
| `Autonomo/pedro/procedures/` + `Tuning.java` | Procedimentos de auto-tuning do Pedro Pathing (`PinpointTuner`, `MecanumTuner`, `ForesightTuner`...). São basicamente código da biblioteca. |
| `Autonomo/pedro/AutoPath.java` | Paths automáticos dentro do TeleOp (ver [seção 3](#3-️-arquitetura-do-robô)). |

---

### 8. 🔧 Atuadores, Sensores e LEDs

| Classe | Hardware (`hardwareMap`) | Tipo | Comportamento |
| :--- | :--- | :--- | :--- |
| `Feeder` | `"feeder"` | `DcMotorEx` | `activate(LB, RB)`: +0.8 / −0.8 / 0. A porta de encoder dele lê o encoder externo do hood (`getHoodPos()`). |
| `MidTake` | `"midTake"` | `DcMotorEx` | `spin(btn1, btn2)`: velocidade fixa (PIDF do Hub) enquanto um dos botões estiver pressionado |
| `HoodServo` | `"hoodServo"` | `CRServo` | PID na posição do encoder externo do hood (zona morta de 15 ticks). Para se o alvo for `NaN`. |
| `GateServo` | `"gateServo"` | `Servo` | Abre o portão do feeder quando o shooter já acelerou |
| `FlowerServo` | `"FlowerServo"` | `CRServo` | `Spin(Ap1)`: 0.5 enquanto o botão estiver pressionado |
| `RampServo` | `"rightRampServo"` | `Servo` | `Position(on)`: aberto 0.83 / fechado 0.4 |
| `TurnOn_TurnOff` | — | utilitário | Detecta a borda de subida do botão e alterna (*toggle*) um booleano |
| `IMU_Initializer` | `"imu"` | `IMU` | Inicializa a IMU do Hub (logo RIGHT, USB BACKWARD) e lê o yaw |

**`ValuesToShoot`:** tabela `distância → {posição do hood, velocidade do shooter}` com interpolação. Entradas `NaN` = ainda não medidas.

#### Sensor de cor: contador de bolas

📄 [`Mech/Color_Sensor.java`](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Mech/Color_Sensor.java) usa o REV Color Sensor V3 `"colorSensor"`.

- Uma bola é detectada quando o **hue** está na faixa `[min, max]` com saturação mínima, **ou** quando a distância é menor que **50 mm**.
- `CountBall(invertido, ...)` conta na borda de subida (bola nova passando), de **0 a 4**. Com `invertido = true`, desconta.
- `ResetBallCounter()` zera; `IsThereBall(...)` só diz se há bola agora.
- Tuning: `Testes/TestColorSensor` (`Test Color sensor`) ajusta `min`, `max` e saturação ao vivo pelo gamepad1.

#### LEDs

| Classe | Hardware | Como funciona |
| :--- | :--- | :--- |
| `Led/RGB_Indicator` | `"Indicator"` (`Servo`) | Indicador RGB controlado por posição de servo: vermelho, laranja, amarelo, verde ou desligado. |
| `Led/Prism` | `"prism"` (`GoBildaPrismDriver`, I2C) | Fita de LED goBILDA Prism: cor sólida e brilho (±10 por clique). Só manda pelo I2C quando algo muda, para não travar o loop. |

`GoBildaPrismDriver`, `PrismAnimations`, `ColorPrism` e `Direction` são código do fabricante (licença MIT, Base 10 Assets).

**Teste integrado:** `Testes/TestLedColorSensor` (`Led&ColorSensor`) liga o contador ao indicador: 0 bolas = apagado, 1 = vermelho, 2 = laranja, 3 = amarelo, 4 = verde. A (liga/desliga) inverte a contagem, options zera.

#### Visão de POLLEN (webcam C270)

📄 [`Mech/c270/PollenDetector.java`](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Mech/c270/PollenDetector.java) é um `VisionProcessor` OpenCV para a webcam `"Webcam 1"`:

1. Máscara de amarelo em HSV e limpeza por morfologia.
2. Cada mancha redonda vira um pólen; manchas grudadas são separadas (`distanceTransform`).
3. Pólens próximos viram grupos (Union-Find) e o maior grupo é escolhido.
4. Calcula o `tx` do grupo dominante.

Roda na thread da câmera e publica um `Result` imutável numa variável `volatile`. Os parâmetros são `public static` e podem ser ajustados ao vivo pelo `TestPollenTracking` (só voltam ao padrão quando o app reinicia).

---

### 9. 🗂️ Estrutura de Pastas

```
MEGA2026-27/
├── FtcRobotController/        # App padrão do SDK da FTC (evitar alterar)
│   └── .../external/samples/  # Exemplos oficiais da FTC
├── TeamCode/src/main/java/org/firstinspires/ftc/teamcode/   (branch Test)
│   ├── Mech/                  # Chassi, FieldCentric, Shooter, Feeder, MidTake, Color_Sensor, LimeLight, PinpointConf...
│   │   ├── Turret/            # TurretConfig, TurretController, TurretTracking, HiveTracking, VisionPoseCorrector
│   │   ├── Servos/            # HoodServo, GateServo, FlowerServo, RampServo (+ ToTurret/: turret por servo, antiga)
│   │   ├── Led/               # RGB_Indicator, Prism + driver goBILDA Prism
│   │   └── c270/              # PollenDetector (OpenCV) e testes da webcam
│   ├── Autonomo/pedro/        # Constants, AutoPath, Tuning + procedures/
│   └── Testes/                # Bancadas de teste manuais (TestTrackingOnChassi, TestFieldAutoPath, TestLedColorSensor...)
├── Limelight/                 # Field map (.fmap) das AprilTags vermelhas
├── doc/                       # Documentação legal e mídia do SDK + README original do SDK
├── build.dependencies.gradle  # Dependências (FTC SDK, Pedro, Panels)
└── build.common.gradle        # Configuração Android compartilhada (não editar)
```

Na branch `main` a estrutura é outra: `TeleOperados/` (TeleOp_BLUE / TeleOp_RED), `Autonomo/pedro/Ivy/`, `PIDS/`, `Another_Codes/` e os subsistemas principais em `Mech/ImportantMech/`.
```

---

### 10. 💻 Instalação e Compilação

**Pré-requisitos:**

- Android Studio (o SDK desta base pede **Ladybug 2024.2 ou mais recente**).
- Um JDK compatível com o Android Gradle Plugin **8.7.0**. O Gradle **9.5.0** é baixado sozinho pelo wrapper.
- Cabo USB ou conexão ADB com o Control Hub.

**Versões identificadas no projeto** (`build.dependencies.gradle`, `build.common.gradle`):

| Componente | Versão |
| :--- | :--- |
| FTC SDK (`RobotCore`, `Hardware`, `Vision`...) | **12.0.0** |
| Pedro Pathing (`core`, `revhub`) | **3.0.0** |
| Pedro Pathing (`telemetry`, `tuning`) | 1.0.0 |
| Pedro Ivy (`com.pedropathing.ivy:pedro`) | **1.1.1** |
| Panels (`com.bylazar:fullpanels`) | 1.0.12 |
| compileSdk / minSdk / targetSdk | 34 / 24 / 28 |
| Java (source/target) | 1.8 |

> ⚠️ A API do Pedro Pathing muda bastante entre versões principais. Escreva o código para as versões acima.

**Comandos** (na raiz do projeto):

```bash
# Build completo (os dois módulos)
./gradlew build

# Só o APK do TeamCode
./gradlew :TeamCode:assembleDebug

# Compila e instala no Control Hub conectado
./gradlew :TeamCode:installDebug
```

Pelo Android Studio, basta abrir a pasta do projeto e usar **Run ▶** com o Control Hub conectado.

> 🧪 **Testes:** não existe suíte de testes automatizados. Testar significa instalar o app e rodar o OpMode no robô pelo Driver Station.

---

### 11. 📚 Recursos de Exemplo da FTC

O repositório inclui a estrutura padrão do SDK da FTC, que traz um conjunto valioso de códigos de amostra. Eles são essenciais para aprendizado, testes e para entender as estruturas básicas de programação.

Os exemplos ficam no módulo `FtcRobotController`, na pasta `src/main/java/org/firstinspires/ftc/robotcontroller/external/samples` (66 arquivos), e cobrem:

* **Amostras de Estrutura:** OpModes básicos de TeleOp e Autônomo (`BasicOpMode_Linear`, `BasicOpMode_Iterative`, `BasicOmniOpMode_Linear`...), que servem de ponto de partida para novos OpModes.
* **Códigos de Sensores:** como inicializar, ler e usar dados de diversos sensores compatíveis com a FTC.
* **Visão:** exemplos de AprilTag com `VisionPortal` (`ConceptAprilTag`, `ConceptAprilTagEasy`...).
* **Códigos de Hardware:** controle de tração, servos e outros atuadores.

Para usar um exemplo, copie-o para o `TeamCode` e remova a anotação `@Disabled`.

📄 O README original do SDK da FTC, com o guia de uso e as notas de versão, foi preservado em [`doc/FTC_SDK_README.md`](doc/FTC_SDK_README.md). A documentação oficial está em [ftc-docs.firstinspires.org](https://ftc-docs.firstinspires.org/).

---

### 12. 📝 Convenções de Código

Todo o código da Mega Snakes deve seguir as convenções de nomenclatura Java e as diretrizes internas da equipe, para manter clareza e facilidade de manutenção:

- **Comentários em português**, curtos e claros. Os nomes de classes e métodos seguem o padrão do arquivo onde estão: o projeto mistura português e inglês.
- **Lógica do robô vai no `TeamCode`**. O `FtcRobotController` e o `build.common.gradle` só mudam se a tarefa for realmente sobre o SDK. Customizações de build vão no `TeamCode/build.gradle`.
- **Nomes no `hardwareMap` precisam bater exatamente com a configuração do robô** no Driver Station. Nomes usados hoje:

  | Nome | Dispositivo |
  | :--- | :--- |
  | `leftFront`, `leftBack`, `rightFront`, `rightBack` | Motores do chassi |
  | `shooter` | Shooter |
  | `turretMotor` | Motor da turret |
  | `feeder`, `midTake` | Motores de alimentação |
  | `hoodServo`, `gateServo`, `FlowerServo`, `rightRampServo` | Servos |
  | `colorSensor` | REV Color Sensor V3 |
  | `Indicator` | Indicador RGB (servo) |
  | `prism` | Fita de LED goBILDA Prism (I2C) |
  | `pinpoint` | GoBilda Pinpoint |
  | `limelight` | Limelight 3A |
  | `Webcam 1` | Webcam C270 |
  | `imu` | IMU do Hub |

  Renomear no código sem atualizar a configuração do robô, ou o contrário, **compila normalmente, mas quebra o OpMode na hora de rodar**.
- **Ângulos em graus** em todo o código de turret e tracking. O Pedro trabalha em radianos, então converta com `Math.toRadians`/`Math.toDegrees` na fronteira.
- **Heading unwrapped vs. normalizado:**
  - O heading usado no feedforward é **unwrapped**: ele é acumulado continuamente e não "salta" de 180° para −180°.
  - O ângulo da turret também é unwrapped (±400°).
  - Quando é preciso ângulo entre −180° e 180°, use os helpers `normalize180`.
  - Não misture as duas formas.
- **Ajustes ficam centralizados:** turret em `TurretConfig` e Pedro em `Autonomo/pedro/Constants.java`.
- **Autônomos novos** usam o estilo **Pedro Ivy** (branch `main`), com uma subpasta por posição inicial.

---

### 13. 🤝 Como Contribuir e Solucionar Problemas

**Fluxo de trabalho:**

1. Atualize sua cópia (`git pull`) e trabalhe numa branch. Branches: `main` (código de competição: TeleOps e autônomos Ivy) e `Test` (reorganização e testes novos: field-centric, AutoPath, sensor de cor, LEDs).
2. Compile antes de subir (`./gradlew :TeamCode:assembleDebug`).
3. Teste no robô e escreva no commit **o que foi testado e como**.
4. OpModes experimentais devem receber `@Disabled` antes de irem para a branch de competição, para não encher a lista do Driver Station.
5. Novos subsistemas seguem o padrão `init(hardwareMap)` + método por loop, dentro de `Mech/`.

**Solução de problemas:**

| Sintoma | O que verificar |
| :--- | :--- |
| OpMode não aparece no Driver Station | Ele está com `@Disabled`? O app foi reinstalado? |
| Erro "unable to find a hardware device" | O nome no `hardwareMap.get` precisa ser idêntico ao da configuração, inclusive maiúsculas e espaços. |
| Turret gira para o lado errado | `TurretConfig.turretReversed` (inverte motor e encoder juntos) e `txSign`. |
| Turret "caçando" em volta do alvo | `deadbandDeg`: abaixo de ~0,68° (1 tick) ela oscila. Depois reduza o `kP`. |
| Mira puxa para um lado com a Limelight | A posição da tag está cadastrada? A telemetria mostra "Pos da tag cadastrada". Confira também o `.fmap`. |
| Limelight sem resultado (`null`) | A telemetria mostra "LL conectada / rodando" e o pipeline pedido. Ela foi ligada com `TurnON` no init? |
| Pose do autônomo diferente da TeleOp | `PinpointConf` e `Autonomo/pedro/Constants.java` têm offsets de pods diferentes. |
| Contador de bolas pulando/errando | Ajuste `min`/`max`/saturação no `Test Color sensor`. A regra de distância (< 50 mm) conta qualquer objeto perto. |
| Tracking errado na aliança azul | Os IDs das tags azuis (`BLUE_*_TAGS`) e o field map azul ainda não foram preenchidos. |

#### Pontos de atenção identificados no código

Estes pontos foram encontrados na leitura do código e **ainda não foram corrigidos**:

- `Mech/LimeLight.java` → `getDistance()`: passa o ângulo em **graus** para `Math.sin`, que espera radianos. A distância sai errada.
- `Mech/LimeLight.java` → `getSin()` (não usado): lê o mapa com chave `double` num mapa de `Integer` e faz unboxing de `null`, o que lança `NullPointerException`.
- `Mech/Feeder.java`: `getHoodPos()` depende do encoder do hood ligado na porta do feeder (`TODO` no código).
- `Mech/Servos/HoodServo.java`: `MaxServoTICKS` ainda não foi medido (`TODO`).
- `Mech/ValuesToShoot.java`: a maioria das entradas ainda é `NaN` (não medida).
- `Autonomo/pedro/AutoPath.java`: o retângulo proibido do meio é um chute.
- Tracking azul: IDs preenchidos, mas sem field map azul e sem teste em campo.


---

<p align="center">🐍 <b>Mega Snakes</b> · FIRST® Tech Challenge · Temporada BIOBUZZ™ 2026–2027 🐝</p>
